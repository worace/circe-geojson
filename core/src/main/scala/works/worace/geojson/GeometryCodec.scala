package works.worace.geojson

import io.circe._
import io.circe.syntax._
import io.circe.generic.extras.auto._
import io.circe.generic.extras.semiauto._
import TypeDiscriminator._
import CoordinateCodec.implicits._
import BBoxCodec.implicits._

object GeometryCodec extends Codec[Geometry] {
  object implicits {
    implicit val geometryEncoder = encoder
    implicit val geometryDecoder = decoder
  }
  private val base: Decoder[Geometry] = deriveConfiguredDecoder[Geometry]
  val decoder: Decoder[Geometry] = Decoder.instance { cursor =>
    decodeWithForeignMembers(cursor, base, (geom, fMembers) => {
      geom match {
        case geom: ForeignMembers[Geometry] => geom.withForeignMembers(fMembers)
        case other                          => other
      }
    })
  }
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

// object GeometryCollectionCodec extends Codec[GeometryCollection] {
//   object implicits {
//     implicit val geometryCollectionEncoder = encoder
//     implicit val geometryCollectionDecoder = decoder
//   }
//   private val base: Decoder[GeometryCollection] = deriveConfiguredDecoder[GeometryCollection]
//   val decoder: Decoder[GeometryCollection] = Decoder.instance { cursor =>
//     decodeWithForeignMembers(cursor, base, (geom, fMembers) => {
//       geom.withForeignMembers(fMembers)
//     })
//   }
// }

object PointCodec extends Codec[Point] {
  object implicits {
    implicit val pointEncoder = encoder
    implicit val pointDecoder = decoder
  }
  private val base: Decoder[Point] = deriveConfiguredDecoder[Point]
  val decoder: Decoder[Point] = Decoder.instance { cursor =>
    decodeWithForeignMembers(cursor, base, (geom, fMembers) => {
      geom.withForeignMembers(fMembers)
    })
  }

  def asJsonObject(point: Point): JsonObject = {
    JsonObject("type" -> "Point".asJson, "coordinates" -> point.coordinates.asJson)
  }
}

object LineStringCodec extends Codec[LineString] {
  object implicits {
    implicit val lineStringEncoder = encoder
    implicit val lineStringDecoder = decoder
  }
  private val base: Decoder[LineString] = deriveConfiguredDecoder[LineString]
  val decoder: Decoder[LineString] = Decoder.instance { cursor =>
    decodeWithForeignMembers(cursor, base, (geom, fMembers) => {
      geom.withForeignMembers(fMembers)
    })
  }

  def asJsonObject(geom: LineString): JsonObject = {
    JsonObject("type" -> "LineString".asJson, "coordinates" -> geom.coordinates.asJson)
  }
}

object PolygonCodec extends Codec[Polygon] {
  object implicits {
    implicit val polyonEncoder = encoder
    implicit val polyonDecoder = decoder
  }
  private val base: Decoder[Polygon] = deriveConfiguredDecoder[Polygon]
  val decoder: Decoder[Polygon] = Decoder.instance { cursor =>
    decodeWithForeignMembers(cursor, base, (geom, fMembers) => {
      geom.withForeignMembers(fMembers)
    })
  }

  def asJsonObject(geom: Polygon): JsonObject = {
    JsonObject("type" -> "Polygon".asJson, "coordinates" -> geom.coordinates.asJson)
  }
}

object MultiPointCodec extends Codec[MultiPoint] {
  object implicits {
    implicit val multiPointEncoder = encoder
    implicit val multiPointDecoder = decoder
  }
  private val base: Decoder[MultiPoint] = deriveConfiguredDecoder[MultiPoint]
  val decoder: Decoder[MultiPoint] = Decoder.instance { cursor =>
    decodeWithForeignMembers(cursor, base, (geom, fMembers) => {
      geom.withForeignMembers(fMembers)
    })
  }

  def asJsonObject(geom: MultiPoint): JsonObject = {
    JsonObject("type" -> "MultiPoint".asJson, "coordinates" -> geom.coordinates.asJson)
  }
}

object MultiLineStringCodec extends Codec[MultiLineString] {
  object implicits {
    implicit val multiLineStringEncoder = encoder
    implicit val multiLineStringDecoder = decoder
  }
  private val base: Decoder[MultiLineString] = deriveConfiguredDecoder[MultiLineString]
  val decoder: Decoder[MultiLineString] = Decoder.instance { cursor =>
    decodeWithForeignMembers(cursor, base, (geom, fMembers) => {
      geom.withForeignMembers(fMembers)
    })
  }

  def asJsonObject(geom: MultiLineString): JsonObject = {
    JsonObject("type" -> "MultiLineString".asJson, "coordinates" -> geom.coordinates.asJson)
  }
}

object MultiPolygonCodec extends Codec[MultiPolygon] {
  object implicits {
    implicit val multiPolygonEncoder = encoder
    implicit val multiPolygonDecoder = decoder
  }
  private val base: Decoder[MultiPolygon] = deriveConfiguredDecoder[MultiPolygon]
  val decoder: Decoder[MultiPolygon] = Decoder.instance { cursor =>
    decodeWithForeignMembers(cursor, base, (geom, fMembers) => {
      geom.withForeignMembers(fMembers)
    })
  }

  def asJsonObject(geom: MultiPolygon): JsonObject = {
    JsonObject("type" -> "MultiPolygon".asJson, "coordinates" -> geom.coordinates.asJson)
  }
}
