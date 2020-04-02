package works.worace.geojson.core

import io.circe.{JsonObject, Decoder, Encoder}
import io.circe.syntax._

trait Codec[T <: GeoJson] {
  def asJsonObject(gj: T): JsonObject
  val encoder: Encoder[T] = Encoder.instance { gj =>
    withBBox(withForeignMembers(asJsonObject(gj), gj.foreignMembers), gj.bbox).asJson
  }
  val decoder: Decoder[T]
  val coreKeys =
    Set("type", "geometry", "coordinates", "properties", "features", "geometries", "id", "bbox")

  def withForeignMembers(encoded: JsonObject, fm: Option[JsonObject]): JsonObject = {
    fm.map { obj =>
      val foreignMembers = obj.filterKeys(!coreKeys.contains(_))
      encoded.deepMerge(foreignMembers)
    }.getOrElse(encoded)
  }

  def withBBox(encoded: JsonObject, bbox: Option[BBox]): JsonObject = {
    import BBoxCodec.implicits._
    bbox.map { bbox =>
      encoded.add("bbox", bbox.asJson)
    }.getOrElse(encoded)
  }
}
