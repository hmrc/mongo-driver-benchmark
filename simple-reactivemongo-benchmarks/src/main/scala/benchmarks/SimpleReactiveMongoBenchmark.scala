package benchmarks

import com.typesafe.config.ConfigFactory
import org.openjdk.jmh.annotations._
import play.api.libs.json.Json
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

  @Benchmark
  def insertSingle(): Unit =
    assert(await(repo.insert(TestObject())).ok)

  @Benchmark
  def insertBulk(): Unit =
    assert(await(repo.bulkInsert(Seq(TestObject(), TestObject(), TestObject()))).ok)

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
      newString = randomUUID().toString
      result <- repo.findAndUpdate(
                  query = Json.obj("aString" -> testObject.aString),
                  update = Json.obj("$set" -> Json.obj("anOption" -> newString)),
                  fetchNewObject = true
                )
    } yield assert(result.result.flatMap(_.anOption).contains(newString)))
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
  def setUp: Unit =
    await(repo.removeAll())

  @TearDown
  def tearDown: Unit =
    mongoConnector.helper.driver.close()

  private def await[T](f: Future[T]): T = Await.result(f, 10.seconds)
}