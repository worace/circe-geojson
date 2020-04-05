package works.worace.geojson.core

import io.circe._
import io.circe.syntax._
import io.circe.generic.extras.auto._
import io.circe.generic.extras.semiauto._
import TypeDiscriminator._
import CoordinateCodec.implicits._

object GeometryCodec extends Codec[Geometry] {
  object implicits {
    implicit val geometryEncoder = encoder
    implicit val geometryDecoder = decoder
  }
  val decoder: Decoder[Geometry] = deriveConfiguredDecoder[Geometry]
  def asJsonObject(gj: Geometry): JsonObject = {
    geometry(gj)
  }

  private def geometryCollection(gc: GeometryCollection): JsonObject = {
    val children = gc.geometries.map(child => Json.fromJsonObject(geometry(child)))
    JsonObject("type" -> "GeometryCollection".asJson, "geometries" -> children.asJson)
  }

  private def geometry(g: Geometry): JsonObject = {
    g match {
      case geom: Point =>
        JsonObject("type" -> geom.`type`.asJson, "coordinates" -> geom.coordinates.asJson)
      case geom: LineString =>
        JsonObject("type" -> geom.`type`.asJson, "coordinates" -> geom.coordinates.asJson)
      case geom: Polygon =>
        JsonObject("type" -> geom.`type`.asJson, "coordinates" -> geom.coordinates.asJson)
      case geom: MultiPoint =>
        JsonObject("type" -> geom.`type`.asJson, "coordinates" -> geom.coordinates.asJson)
      case geom: MultiLineString =>
        JsonObject("type" -> geom.`type`.asJson, "coordinates" -> geom.coordinates.asJson)
      case geom: MultiPolygon =>
        JsonObject("type" -> geom.`type`.asJson, "coordinates" -> geom.coordinates.asJson)
      case geom: GeometryCollection => geometryCollection(geom)
    }
  }
}
