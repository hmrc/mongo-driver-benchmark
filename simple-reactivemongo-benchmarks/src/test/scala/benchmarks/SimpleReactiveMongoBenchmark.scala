package benchmarks

import org.scalameter.Bench.LocalTime
import org.scalameter.Gen
import play.api.libs.json.Json
import repository.TestObject.formats
import repository.{TestObject, TestRepository}
import uk.gov.hmrc.mongo.MongoConnector

import java.util.UUID.randomUUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

trait SimpleReactiveMongoBenchmark extends LocalTime {

  private val repo = new TestRepository(MongoConnector("mongodb://localhost:27017/benchmarks-test"))

  performance of "simple-reactivemongo" in {

    performance of "insert" in {
      measure method "insert single " in {
        using(range) in { _ =>
          assert(await(repo.insert(TestObject())).ok)
        }
      }

      measure method "insert bulk " in {
        using(range) in { _ =>
          val testObject1 = TestObject()
          val testObject2 = TestObject()
          val testObject3 = TestObject()
          assert(await(repo.bulkInsert(Seq(testObject1, testObject2, testObject3))).ok)
        }
      }
    }

    performance of "querying" in {
      measure method "find" in {
        using(range) in { _ =>
          val testObject = TestObject()
          await(for {
            _      <- repo.insert(testObject)
            result <- repo.find("aString" -> testObject.aString)
          } yield assert(result.size == 1))
        }
      }

      measure method "findById" in {
        using(range) in { _ =>
          val testObject = TestObject()
          await(for {
            _      <- repo.insert(testObject)
            result <- repo.findById(testObject.id)
          } yield assert(result.isDefined))
        }
      }
    }

    performance of "update" in {
      measure method "find and update indexed field" in {
        using(range) in { _ =>
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
      }

      measure method "find and update non indexed field" in {
        using(range) in { _ =>
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
      }
    }

    performance of "count" in {
      measure method "count" in {
        using(range) in { _ =>
          await(for {
            _      <- repo.insert(TestObject())
            result <- repo.count
          } yield assert(result > 0))
        }
      }

      measure method "count by query" in {
        using(range) in { _ =>
          val testObject = TestObject()
          await(for {
            _      <- repo.insert(testObject)
            result <- repo.count(Json.obj("aString" -> testObject.aString))
          } yield assert(result > 0))
        }
      }
    }

    performance of "delete" in {
      measure method "remove by query" in {
        using(range) in { _ =>
          val testObject = TestObject()
          await(for {
            _      <- repo.insert(testObject)
            result <- repo.remove("aString" -> testObject.aString)
          } yield assert(result.ok))
        }
      }

      measure method "remove by id" in {
        using(range) in { _ =>
          val testObject = TestObject()
          await(for {
            _      <- repo.insert(testObject)
            result <- repo.removeById(testObject.id)
          } yield assert(result.ok))
        }
      }
    }
  }

  private def range: Gen[Int] = Gen.range("count")(from = 1, upto = 10, hop = 1)

  private def await[T](f: Future[T]): T = Await.result(f, 5.seconds)

}
