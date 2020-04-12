package works.worace.geojson

import io.circe._
import io.circe.Decoder.Result
import io.circe.syntax._

private trait Codec[T <: GeoJson] {
  val encoder: Encoder[T] = Encoder.instance(_.asJsonObject.asJson)
  val decoder: Decoder[T]

  protected def decodeWithForeignMembers[T <: GeoJson](
    cursor: HCursor,
    baseDecoder: Decoder[T],
    handler: (T, Option[JsonObject]) => T
  ): Result[T] = {
    cursor.as[JsonObject].flatMap { obj =>
      Json
        .fromJsonObject(obj)
        .as[T](baseDecoder)
        .map { base => handler(base, foreignMembers(obj)) }
    }
  }

  protected def foreignMembers(json: JsonObject): Option[JsonObject] = {
    val foreignMembers = json.filterKeys(!GeoJson.coreKeys.contains(_))
    if (foreignMembers.nonEmpty) {
      Some(foreignMembers)
    } else {
      None
    }
  }
}
