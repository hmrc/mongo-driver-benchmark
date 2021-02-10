package benchmarks

import com.typesafe.config.ConfigFactory
import org.openjdk.jmh.annotations._
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import repository.{SimpleReactiveMongoRepository, TestObject}
import uk.gov.hmrc.mongo.MongoConnector

import java.util.UUID.randomUUID
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

@State(Scope.Benchmark)
class SimpleReactiveMongoBenchmark {

  import TestObject.formats

  private implicit lazy val ec    = ExecutionContext.Implicits.global
  private lazy val mongoUri       = ConfigFactory.load().getString("mongo.uri")
  private lazy val mongoConnector = MongoConnector(mongoUri)
  private lazy val repo           = new SimpleReactiveMongoRepository(mongoConnector)

  private val knownString   = randomUUID().toString
  private val knownObjectId = BSONObjectID.generate()

  @Benchmark
  def insertSingle(): Unit =
    assertF(repo.insert(TestObject()))(_.ok)

  @Benchmark
  def insertBulk(): Unit =
    assertF(repo.bulkInsert(Seq(TestObject(), TestObject(), TestObject())))(_.ok)

  @Benchmark
  def find(): Unit =
    assertF(repo.find("aString" -> knownString))(_.size == 1)

  @Benchmark
  def findById(): Unit =
    assertF(repo.findById(knownObjectId))(_.isDefined)

  @Benchmark
  def findAndUpdateIndexedField(): Unit =
    assertF(
      repo.findAndUpdate(
        query = Json.obj("aString" -> knownString),
        update = Json.obj("$set" -> Json.obj("aString" -> knownString)),
        fetchNewObject = true
      )
    )(_.result.exists(_.aString == knownString))

  @Benchmark
  def findAndUpdateNonIndexedField(): Unit = {
    val newString = randomUUID().toString
    assertF(
      repo.findAndUpdate(
        query = Json.obj("aString" -> knownString),
        update = Json.obj("$set" -> Json.obj("anOption" -> newString)),
        fetchNewObject = true
      )
    )(_.result.flatMap(_.anOption).contains(newString))
  }

  @Benchmark
  def count(): Unit =
    assertF(repo.count)(_ > 0)

  @Benchmark
  def countByQuery(): Unit =
    assertF(repo.count(Json.obj("aString" -> knownString)))(_ > 0)

  @Benchmark
  def remove(): Unit =
    assertF(repo.remove("aString" -> knownString))(_.ok)

  @Benchmark
  def removeById(): Unit =
    assertF(repo.removeById(knownObjectId))(_.ok)

  @Setup
  def setUp: Unit =
    await(for {
      _ <- repo.removeAll()
      _ <- repo.bulkInsert(
             (1 to 1000).map(_ => TestObject()) ++ Seq(
               TestObject(aString = knownString),
               TestObject(id = knownObjectId)
             )
           )
    } yield ())

  @TearDown
  def tearDown: Unit =
    mongoConnector.helper.driver.close()

  private def assertF[T](f: Future[T])(predicate: T => Boolean) =
    assert(predicate(await(f)))

  private def await[T](f: Future[T]): T = Await.result(f, 10.seconds)
}