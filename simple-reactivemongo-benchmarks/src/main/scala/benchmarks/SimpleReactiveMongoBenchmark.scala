package benchmarks

import org.openjdk.jmh.annotations._
import play.api.libs.json.Json
import reactivemongo.ReactiveMongoHelper
import repository.{TestObject, TestRepository}
import uk.gov.hmrc.mongo.MongoConnector

import java.util.UUID.randomUUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import TestObject.formats

@State(Scope.Benchmark)
class SimpleReactiveMongoBenchmark {

  var mongoConnector: MongoConnector = _
  var repo: TestRepository           = _

  @Benchmark
  def insertSingle(): Unit =
    assert(await(repo.insert(TestObject())).ok)

  @Benchmark
  def insertBulk(): Unit =
    assert(await(repo.insert(TestObject())).ok)

  @Benchmark
  def find(): Unit = {
    val testObject = TestObject()
    await(for {
      _      <- repo.insert(testObject)
      result <- repo.find("aString" -> testObject.aString)
    } yield assert(result.size == 1))
  }

  @Benchmark
  def findById(): Unit = {
    val testObject = TestObject()
    await(for {
      _      <- repo.insert(testObject)
      result <- repo.findById(testObject.id)
    } yield assert(result.isDefined))
  }

  @Benchmark
  def findAndUpdateIndexedField(): Unit = {
    val testObject = TestObject()
    await(for {
      _ <- repo.insert(testObject)
      newString = randomUUID().toString
      result <- repo.findAndUpdate(
                  query = Json.obj("aString" -> testObject.aString),
                  update = Json.obj("$set" -> Json.obj("aString" -> newString)),
                  fetchNewObject = true
                )
    } yield assert(result.result.exists(_.aString == newString)))
  }

  @Benchmark
  def findAndUpdateNonIndexedField(): Unit = {
    val testObject = TestObject()
    await(for {
      _ <- repo.insert(testObject)
      result <- repo.findAndUpdate(
                  query = Json.obj("aString" -> testObject.aString),
                  update = Json.obj("$set" -> Json.obj("anOption" -> "a new string")),
                  fetchNewObject = true
                )
    } yield assert(result.result.flatMap(_.anOption).contains("a new string")))
  }

  @Benchmark
  def count(): Unit =
    await(for {
      _      <- repo.insert(TestObject())
      result <- repo.count
    } yield assert(result > 0))

  @Benchmark
  def countByQuery(): Unit = {
    val testObject = TestObject()
    await(for {
      _      <- repo.insert(testObject)
      result <- repo.count(Json.obj("aString" -> testObject.aString))
    } yield assert(result > 0))
  }

  @Benchmark
  def remove(): Unit = {
    val testObject = TestObject()
    await(for {
      _      <- repo.insert(testObject)
      result <- repo.remove("aString" -> testObject.aString)
    } yield assert(result.ok))
  }

  @Benchmark
  def removeById(): Unit = {
    val testObject = TestObject()
    await(for {
      _      <- repo.insert(testObject)
      result <- repo.removeById(testObject.id)
    } yield assert(result.ok))

  }

  @Setup
  def setUp: Unit = {
    mongoConnector = MongoConnector("mongodb://localhost:27017/benchmarks-test")
    repo = new TestRepository(mongoConnector)
  }

  @TearDown
  def tearDown: Unit = {
    Await.result(repo.removeAll(), 5.seconds)
    Await.result(
      mongoConnector.helper.driver.system.terminate(),
      mongoConnector.dbTimeout.getOrElse(ReactiveMongoHelper.DEFAULT_DB_TIMEOUT)
    )
  }

  private def await[T](f: Future[T]): T = Await.result(f, 5.seconds)
}
