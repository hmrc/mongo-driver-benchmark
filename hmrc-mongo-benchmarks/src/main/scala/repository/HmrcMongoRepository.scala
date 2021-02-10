package repository

import org.joda.time.{DateTime, DateTimeZone}
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsValue, Json, __}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.{MongoFormats, MongoJavatimeFormats}

import java.time.Instant
import java.util.UUID.randomUUID
import scala.concurrent.ExecutionContext

case class CreationAndLastModifiedDetail(
  createdAt: DateTime = DateTime.now.withZone(DateTimeZone.UTC),
  lastUpdated: DateTime = DateTime.now.withZone(DateTimeZone.UTC)
)

object CreationAndLastModifiedDetail {
  implicit val dateTimeFormat: Format[DateTime] =
    uk.gov.hmrc.mongo.play.json.formats.MongoJodaFormats.Implicits.jotDateTimeFormats
  implicit val formats = Json.format[CreationAndLastModifiedDetail]
}

case class Primitives(string: String = "a string", int: Int = 100, long: Long = 100L, boolean: Boolean = true)

object Primitives {
  implicit val formats = Json.format[Primitives]
}

case class TestObject(
  aString: String = randomUUID().toString,
  anOption: Option[String] = Some("an option"),
  aNone: Option[String] = None,
  optionalCollection: Option[List[Primitives]] = Some(List(Primitives(), Primitives(), Primitives())),
  nestedMapOfCollections: Map[String, List[Map[String, Seq[Primitives]]]] = Map(
    "key1" -> List(Map("key1-keya" -> List(Primitives()), "key1-keyb" -> List(Primitives(), Primitives()))),
    "key2" -> List(Map("key2-keya" -> List(Primitives(), Primitives()), "key2-keyb" -> List(Primitives())))
  ),
  modifiedDetails: CreationAndLastModifiedDetail = CreationAndLastModifiedDetail(),
  anInstant: Instant = Instant.now(),
  jsValue: Option[JsValue] = Some(Json.toJson(Primitives())),
  location: (Double, Double) = (10, 20),
  id: ObjectId = new ObjectId()
)

object TestObject {

  implicit val formats = {
    implicit val objectIdFormats: Format[ObjectId]    = MongoFormats.objectIdFormats
    implicit val primitivesFormat: Format[Primitives] = Json.format[Primitives]
    implicit val instf: Format[Instant]               = MongoJavatimeFormats.instantFormats
    (
      (__ \ "aString").format[String]
        ~ (__ \ "anOption").formatNullable[String]
        ~ (__ \ "aNone").formatNullable[String]
        ~ (__ \ "optionalCollection").formatNullable[List[Primitives]]
        ~ (__ \ "nestedMapOfCollections").format[Map[String, List[Map[String, Seq[Primitives]]]]]
        ~ (__ \ "modifiedDetails").format[CreationAndLastModifiedDetail]
        ~ (__ \ "anInstant").format[Instant]
        ~ (__ \ "jsValue").formatNullable[JsValue]
        ~ (__ \ "location").format[(Double, Double)]
        ~ (__ \ "_id").format[ObjectId]
    )(TestObject.apply, unlift(TestObject.unapply))
  }
}

class HmrcMongoRepository(mongoComponent: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[TestObject](
      collectionName = "hmrcMongo",
      mongoComponent = mongoComponent,
      domainFormat = TestObject.formats,
      indexes = Seq(
        IndexModel(ascending("aString"), IndexOptions().name("aStringUniqueIdx").unique(true))
      )
    ) {}
