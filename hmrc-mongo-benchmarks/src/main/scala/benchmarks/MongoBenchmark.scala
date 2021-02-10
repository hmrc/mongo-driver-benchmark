package benchmarks

import com.mongodb.client.model.ReturnDocument
import com.typesafe.config.ConfigFactory
import org.mongodb.scala.bson.{BsonDocument, ObjectId}
import org.mongodb.scala.model.Filters.{equal, notEqual}
import org.mongodb.scala.model.Updates.set
import org.mongodb.scala.model.{FindOneAndUpdateOptions, InsertOneModel}
import org.openjdk.jmh.annotations._
import repository.{HmrcMongoRepository, TestObject}
import uk.gov.hmrc.mongo.MongoComponent

import java.util.UUID.randomUUID
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

@State(Scope.Benchmark)
class MongoBenchmark {

  private implicit lazy val ec    = ExecutionContext.Implicits.global
  private lazy val mongoUri       = ConfigFactory.load().getString("mongo.uri")
  private lazy val mongoComponent = MongoComponent(mongoUri)
  private lazy val repository     = new HmrcMongoRepository(mongoComponent)
  private lazy val collection     = repository.collection

  private val knownString   = randomUUID().toString
  private val knownObjectId = new ObjectId()

  @Benchmark
  def insertSingle(): Unit =
    assertF(collection.insertOne(TestObject()).toFuture())(_.wasAcknowledged())

  @Benchmark
  def insertBulk(): Unit =
    assertF(collection.bulkWrite((1 to 10).map(_ => InsertOneModel(TestObject()))).toFuture())(_.wasAcknowledged())

  @Benchmark
  def find(): Unit =
    assertF(collection.find(filter = equal("aString", knownString)).toFuture())(_.size == 1)

  @Benchmark
  def findById(): Unit =
    assertF(collection.find(equal("_id", knownObjectId)).toFuture())(_.nonEmpty)

  @Benchmark
  def findAndUpdateIndexedField(): Unit =
    assertF(
      collection
        .findOneAndUpdate(
          filter = equal("aString", knownString),
          update = set("aString", knownString),
          options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )
        .toFutureOption()
    )(_.exists(_.aString == knownString))

  @Benchmark
  def findAndUpdateNonIndexedField(): Unit = {
    val newString = randomUUID().toString
    assertF(
      collection
        .findOneAndUpdate(
          filter = equal("aString", knownString),
          update = set("anOption", newString),
          options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )
        .toFutureOption()
    )(_.flatMap(_.anOption).contains(newString))
  }

  @Benchmark
  def count(): Unit =
    assertF(collection.countDocuments().toFuture())(_ > 0)

  @Benchmark
  def countByQuery(): Unit =
    assertF(collection.countDocuments(filter = equal("aString", knownString)).toFuture())(_ > 0)

  @Benchmark
  def remove(): Unit =
    assertF(collection.deleteOne(filter = notEqual("aString", knownString)).toFuture())(_.wasAcknowledged())

  @Benchmark
  def removeById(): Unit =
    assertF(collection.deleteOne(notEqual("_id", knownObjectId)).toFuture())(_.wasAcknowledged())

  @Setup
  def setup: Unit =
    await(for {
      _ <- collection.deleteMany(filter = BsonDocument()).toFuture()
      _ <- collection
             .bulkWrite(
               (1 to 1000).map(_ => InsertOneModel(TestObject())) ++ Seq(
                 InsertOneModel(TestObject(aString = knownString)),
                 InsertOneModel(TestObject(id = knownObjectId))
               )
             )
             .toFuture()

    } yield ())

  @TearDown
  def tearDown: Unit =
    mongoComponent.client.close()

  private def assertF[T](f: Future[T])(predicate: T => Boolean) =
    assert(predicate(await(f)))

  private def await[T](f: Future[T]): T = Await.result(f, 30.seconds)
}
