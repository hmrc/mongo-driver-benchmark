package benchmarks

import com.mongodb.client.model.ReturnDocument
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.FindOneAndUpdateOptions
import org.mongodb.scala.model.Updates.set
import org.openjdk.jmh.annotations._
import repository.{TestObject, HmrcMongoRepository}
import uk.gov.hmrc.mongo.MongoComponent

import java.util.UUID.randomUUID
import java.util.concurrent.Executors
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

@State(Scope.Benchmark)
class HmrcMongoBenchmark {

  private var mongoComponent: MongoComponent          = _
  private var collection: MongoCollection[TestObject] = _
  private implicit var ec: ExecutionContext           = _

  @Benchmark
  def insertSingle(): Unit =
    assert(await(collection.insertOne(TestObject()).toFuture()).wasAcknowledged())

  @Benchmark
  def insertBulk(): Unit =
    assert(await(collection.insertMany(Seq(TestObject(), TestObject(), TestObject())).toFuture()).wasAcknowledged())

  @Benchmark
  def find(): Unit = {
    val testObject = TestObject()
    await(for {
      _      <- collection.insertOne(testObject).toFuture()
      result <- collection.find(filter = equal("aString", testObject.aString)).toFuture()
    } yield assert(result.size == 1))
  }

  @Benchmark
  def findById(): Unit = {
    val testObject = TestObject()
    await(for {
      _      <- collection.insertOne(testObject).toFuture()
      result <- collection.find(equal("_id", testObject.id)).toFuture()
    } yield assert(result.nonEmpty))
  }

  @Benchmark
  def findAndUpdateIndexedField(): Unit = {
    val testObject = TestObject()
    await(for {
      _ <- collection.insertOne(testObject).toFuture()
      newString = randomUUID().toString
      result <- collection
                  .findOneAndUpdate(
                    filter = equal("aString", testObject.aString),
                    update = set("aString", newString),
                    options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
                  )
                  .toFutureOption()
    } yield assert(result.exists(_.aString == newString)))
  }

  @Benchmark
  def findAndUpdateNonIndexedField(): Unit = {
    val testObject = TestObject()
    await(for {
      _ <- collection.insertOne(testObject).toFuture()
      newString = randomUUID().toString
      result <- collection
                  .findOneAndUpdate(
                    filter = equal("aString", testObject.aString),
                    update = set("anOption", newString),
                    options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
                  )
                  .toFutureOption()
    } yield assert(result.flatMap(_.anOption).contains(newString)))
  }

  @Benchmark
  def count(): Unit =
    await(for {
      _      <- collection.insertOne(TestObject()).toFuture()
      result <- collection.countDocuments().toFuture()
    } yield assert(result > 0))

  @Benchmark
  def countByQuery(): Unit = {
    val testObject = TestObject()
    await(for {
      _      <- collection.insertOne(testObject).toFuture()
      result <- collection.countDocuments(filter = equal("aString", testObject.aString)).toFuture()
    } yield assert(result > 0))
  }

  @Benchmark
  def remove(): Unit = {
    val testObject = TestObject()
    await(for {
      _      <- collection.insertOne(testObject).toFuture()
      result <- collection.deleteOne(filter = equal("aString", testObject.aString)).toFuture()
    } yield assert(result.wasAcknowledged()))
  }

  @Benchmark
  def removeById(): Unit = {
    val testObject = TestObject()
    await(for {
      _      <- collection.insertOne(testObject).toFuture()
      result <- collection.deleteOne(equal("_id", testObject.id)).toFuture()
    } yield assert(result.wasAcknowledged()))
  }

  @Setup
  def setUp: Unit = {
    ec = ExecutionContext.fromExecutor(Executors.newWorkStealingPool())
    mongoComponent = MongoComponent("mongodb://localhost:27017/benchmarks-test")
    val repository = new HmrcMongoRepository(mongoComponent)
    collection = repository.collection
  }

  @TearDown
  def tearDown: Unit = {
    await(collection.deleteMany(filter = BsonDocument()).toFuture())
    mongoComponent.client.close()
  }

  private def await[T](f: Future[T]): T = Await.result(f, 5.seconds)
}