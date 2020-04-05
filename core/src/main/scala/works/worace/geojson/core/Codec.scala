package works.worace.geojson.core

import io.circe._
import io.circe.Decoder.Result
import io.circe.syntax._

trait Codec[T <: GeoJson] {
  def asJsonObject(gj: T): JsonObject
  val encoder: Encoder[T] = Encoder.instance { gj =>
    withBBox(withForeignMembers(asJsonObject(gj), gj.foreignMembers), gj.bbox).asJson
  }
  val decoder: Decoder[T]

  def decodeWithForeignMembers[T <: GeoJson](cursor: HCursor, baseDecoder: Decoder[T], handler: (T, Option[JsonObject]) => T): Result[T] = {
    cursor.as[JsonObject].flatMap { obj =>
      Json.fromJsonObject(obj)
        .as[T](baseDecoder)
        .map { base =>
          handler(base, foreignMembers(obj))
        }
    }
  }

  val coreKeys =
    Set("type", "geometry", "coordinates", "properties", "features", "geometries", "id", "bbox")

  def foreignMembers(json: JsonObject): Option[JsonObject] = {
    val foreignMembers = json.filterKeys(!coreKeys.contains(_))
    if (foreignMembers.nonEmpty) {
      Some(foreignMembers)
    } else {
      None
    }
  }

  def withForeignMembers(encoded: JsonObject, fm: Option[JsonObject]): JsonObject = {
    fm.map { obj =>
        val foreignMembers = obj.filterKeys(!coreKeys.contains(_))
        encoded.deepMerge(foreignMembers)
      }
      .getOrElse(encoded)
  }

  def withBBox(encoded: JsonObject, bbox: Option[BBox]): JsonObject = {
    import BBoxCodec.implicits._
    bbox.map { bbox => encoded.add("bbox", bbox.asJson) }.getOrElse(encoded)
  }
}
