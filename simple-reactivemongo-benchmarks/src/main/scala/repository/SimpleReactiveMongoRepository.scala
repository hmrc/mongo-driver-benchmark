package repository

import play.api.libs.json.{Format, JsValue, Json, OFormat}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.json.{ReactiveMongoFormats, TupleFormats}
import uk.gov.hmrc.mongo.{CreationAndLastModifiedDetail, MongoConnector, ReactiveRepository}

import java.time.Instant
import reactivemongo.api.indexes.IndexType
import reactivemongo.api.indexes.Index

import java.util.UUID.randomUUID

case class Primitives(string: String = "a string", int: Int = 100, long: Long = 100L, boolean: Boolean = true)

object Primitives{
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
  id: BSONObjectID = BSONObjectID.generate
)

object TestObject {

  import uk.gov.hmrc.mongo.json.ReactiveMongoFormats.{mongoEntity, objectIdFormats}

  implicit val formats = mongoEntity {
    implicit val locationFormat: Format[(Double, Double)] = TupleFormats.tuple2Format[Double, Double]
    implicit val nestedModelformats: OFormat[Primitives]  = Json.format[Primitives]
    Json.format[TestObject]
  }
}

class SimpleReactiveMongoRepository(mongoConnector: MongoConnector)
    extends ReactiveRepository[TestObject, BSONObjectID](
      "simpleReactiveMongo",
      mongoConnector.db,
      TestObject.formats,
      ReactiveMongoFormats.objectIdFormats
    ) {

  override lazy val indexes: Seq[Index] = Seq(
    Index(Seq("aString" -> IndexType.Ascending), name = Some("aStringUniqueIdx"), unique = true)
  )
}
