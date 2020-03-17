package works.worace.geojson.core

import io.circe._
// import io.circe.generic.auto._
import io.circe.parser._
// import io.circe.syntax._

object GeoJson {
  import io.circe.syntax._
  import io.circe.generic.extras.Configuration
  import GeoJsonSerde._

  def parse(rawJson: String): Either[io.circe.Error, GeoJson] = {
    println("decode json")
    println(rawJson)
    decode[GeoJson](rawJson)(GeoJsonSerde.decoder)
  }
}

object IdSerde {
  type ID = Either[Long, String]

  // https://github.com/circe/circe/issues/672
  implicit def encodeEither[A, B](implicit
    encoderA: Encoder[A],
    encoderB: Encoder[B]
  ): Encoder[Either[A, B]] = {
    import io.circe.syntax._
    o: Either[A, B] =>
      o.fold(_.asJson, _.asJson)
  }

  implicit def decodeEither[A, B](
    implicit
    decoderA: Decoder[A],
    decoderB: Decoder[B]
  ): Decoder[Either[A, B]] = decoderA.either(decoderB)
}

case class Coordinate(x: Double, y: Double, z: Option[Double], m: Option[Double]) {
  def arr: Array[Double] = {
    this match {
      case Coordinate(x, y, None, None)       => Array(x, y)
      case Coordinate(x, y, Some(z), None)    => Array(x, y, z)
      case Coordinate(x, y, Some(z), Some(m)) => Array(x, y, z, m)
      // TODO: What is right here? Should ideally prevent constructing Coord with x,y,m but no z
      case Coordinate(x, y, None, Some(_)) => Array(x, y)
    }
  }
}

object Coordinate {
  def apply(x: Double, y: Double): Coordinate = {
    Coordinate(x, y, None, None)
  }

  def apply(x: Double, y: Double, z: Double): Coordinate = {
    Coordinate(x, y, Some(z), None)
  }
}

object CoordinateSerde {
  implicit val encodeCoord: Encoder[Coordinate] = Encoder.instance {
    // TODO - how does this handle large numbers that circe can't represent as JsonNumber
    coord =>
      Json.arr(coord.arr.flatMap(Json.fromDouble): _*)
  }

  implicit val decodeCoord: Decoder[Coordinate] = new Decoder[Coordinate] {
    final def apply(c: HCursor): Decoder.Result[Coordinate] = {
      println("decode coord")
      println(c)
      println(c.as[Array[Double]])
      c.as[Array[Double]]
        .filterOrElse(
          coords => coords.size > 1 && coords.size < 5,
          DecodingFailure("Invalid GeoJson Coordinates", c.history)
        )
        .map {
          case Array(x, y, z, m) => Coordinate(x, y, Some(z), Some(m))
          case Array(x, y, z)    => Coordinate(x, y, Some(z), None)
          case Array(x, y)       => Coordinate(x, y, None, None)
        }
    }
  }
}

object GeoJsonSerde {
  // TODO:
  // * [x] Replace this with `type` member on GeoJson trait
  // * [x] Widen Geometry Serde to GeoJsonSerde and handle all cases here
  // * [ ] since foreign members + bbox apply to all, this should be OK

  def geomCollectionBase(gc: GeometryCollection): JsonObject = {
    import io.circe.syntax._
    val children = gc.geometries.map(child => Json.fromJsonObject(geomBase(child)))
    JsonObject("geometries" -> children.asJson)
  }

  def geomBase(g: Geometry): JsonObject = {
    import io.circe.syntax._
    import CoordinateSerde._
    g match {
      case geom: Point => JsonObject("coordinates" -> geom.coordinates.asJson)
      case geom: LineString => JsonObject("coordinates" -> geom.coordinates.asJson)
      case geom: Polygon => JsonObject("coordinates" -> geom.coordinates.asJson)
      case geom: MultiPoint => JsonObject("coordinates" -> geom.coordinates.asJson)
      case geom: MultiLineString => JsonObject("coordinates" -> geom.coordinates.asJson)
      case geom: MultiPolygon => JsonObject("coordinates" -> geom.coordinates.asJson)
      case geom: GeometryCollection => geomCollectionBase(geom)
    }
  }

  def featureBase(f: Feature): JsonObject = {
    val geom = JsonObject("geometry" -> Json.fromJsonObject(geomBase(f.geometry)))
    f.properties match {
      case None => geom
      case Some(props) => geom.add("properties", Json.fromJsonObject(props))
    }
  }

  def featureCollectionBase(fc: FeatureCollection): JsonObject = {
    import io.circe.syntax._
    JsonObject("features" -> fc.features.map(featureBase).asJson)
    // val features = JsonObject("features" -> fc.features)
    // f.bbox match {
    //   case None => features
    //   case Some(props) => features.add("bbox", Json.fromJsonObject(props))
    // }
  }

  def base(g: GeoJson): JsonObject = {
    import io.circe.syntax._
    g match {
      case geom: Geometry => geomBase(geom)
      case f: Feature => featureBase(f)
      case fc: FeatureCollection => featureCollectionBase(fc)
    }
  }

  implicit val encodeGeoJson: Encoder[GeoJson] = Encoder.instance {
    gj => Json.fromJsonObject(base(gj).add("type", Json.fromString(gj.`type`)))
  }

  val coreKeys = Set("type", "geometry", "coordinates", "properties", "features")

  object Base {
    import io.circe.generic.extras.auto._
    import io.circe.generic.extras.Configuration
    implicit val geojsonTypeDiscriminator: Configuration =
      Configuration.default.withDiscriminator("type")
    val decoder: Decoder[GeoJson] = new Decoder[GeoJson] {
      final def apply(c: HCursor): Decoder.Result[GeoJson] = {
        import CoordinateSerde._
        import IdSerde._
        c.as[GeoJson]
      }
    }
  }

  val decoder: Decoder[GeoJson] = new Decoder[GeoJson] {
    final def apply(c: HCursor): Decoder.Result[GeoJson] = {
      println("decode geom")
      println(c)

      println("as JsonObject res:")
      println(c.as[JsonObject])
      println("as GeoJson res:")
      println(c.as[GeoJson](Base.decoder))
val base = c.as[GeoJson](Base.decoder)

      c.as[JsonObject]
        .flatMap { obj =>
          val base = Json.fromJsonObject(obj).as[GeoJson](Base.decoder)
          println("got base")
          println(base)
          // get foreign members: keys other than type, coordinates, geometry
          val foreignMembers = obj.filterKeys(!coreKeys.contains(_))
          println("has foreign mebers:")
          println(foreignMembers)
          base
        }
    }
  }
}

// Q: Separate ADT for decoders / formats vs. public interface?
// MultiPoint(Seq[Point])
// vs MultiPoint(type: String, coordinates: Seq[Coordinate])

case class BBox(min: Coordinate, max: Coordinate)

sealed trait Geometry

sealed trait GeoJson {
  val foreignMembers: Option[JsonObject]
  val bbox: Option[BBox]
  def `type`: String
}

case class Point(
  coordinates: Coordinate,
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends GeoJson with Geometry {
  val `type` = "Point"
}
case class LineString(
  coordinates: Vector[Coordinate],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends GeoJson
    with Geometry {
  val `type` = "LineString"
}
case class Polygon(
  coordinates: Vector[Vector[Coordinate]],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends GeoJson
    with Geometry {
  val `type` = "Polygon"
}
case class MultiPoint(
  coordinates: Vector[Coordinate],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends GeoJson
    with Geometry {
  val `type` = "MultiPoint"
}
case class MultiLineString(
  coordinates: Vector[Vector[Coordinate]],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends GeoJson
    with Geometry {
  val `type` = "MultiLineString"
}
case class MultiPolygon(
  coordinates: Vector[Vector[Vector[Coordinate]]],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends GeoJson
    with Geometry {
  val `type` = "MultiPolygon"
}
case class GeometryCollection(
  geometries: Vector[Geometry],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends GeoJson
    with Geometry {
  val `type` = "GeometryCollection"
}
case class Feature(
  id: Option[Either[JsonNumber, String]],
  properties: Option[JsonObject],
  geometry: Geometry,
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends GeoJson {
  val `type` = "Feature"
}

case class FeatureCollection(
  features: Vector[Feature],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends GeoJson {
  val `type` = "FeatureCollection"
}

object Point {
  def apply(coord: Coordinate): Point = Point(coord, None, None)
  def apply(x: Double, y: Double): Point = Point(Coordinate(x, y))
}

object LineString {
  def apply(coords: Seq[Coordinate]): LineString = LineString(coords.toVector, None, None)
}

object Polygon {
  def apply(coords: Seq[Seq[Coordinate]]): Polygon =
    Polygon(coords.map(_.toVector).toVector, None, None)
}

object MultiPoint {
  def apply(coords: Seq[Coordinate]): MultiPoint = MultiPoint(coords.toVector, None, None)
}

object MultiLineString {
  def apply(coords: Seq[Seq[Coordinate]]): MultiLineString =
    MultiLineString(coords.map(_.toVector).toVector, None, None)
}

object MultiPolygon {
  def apply(coords: Seq[Seq[Seq[Coordinate]]]): MultiPolygon =
    MultiPolygon(coords.map(_.map(_.toVector).toVector).toVector, None, None)
}

object GeometryCollection {
  def apply(geometries: Seq[Geometry]): GeometryCollection =
    GeometryCollection(geometries.toVector, None, None)
}

object Feature {
  def apply(geometry: Geometry): Feature = Feature(None, None, geometry, None, None)
  def apply(properties: JsonObject, geometry: Geometry): Feature =
    Feature(None, Some(properties), geometry, None, None)
  def apply(id: String, properties: JsonObject, geometry: Geometry): Feature =
    Feature(Some(Right(id)), Some(properties), geometry, None, None)
  def apply(id: Int, properties: JsonObject, geometry: Geometry): Feature =
    Feature(Some(Left(Json.fromInt(id).asNumber.get)), Some(properties), geometry, None, None)
}

object FeatureCollection {
  def apply(features: Seq[Feature]): FeatureCollection =
    FeatureCollection(features.toVector, None, None)
}

// Goals
// Coordinates
// * [x] 2d
// * [x] 3d
// * [x] 4d
// * [ ] Test: fails with too few and too many dimensions
// Basic Geometries
// * [x] Point
// * [x] LineString
// * [x] Polygon
// * [x] MultiPoint
// * [x] MultiLineString
// * [x] MultiPolygon
// * [x] GeometryCollection
// * [ ] Feature
//   * [x] id
//     * [x] Nullable
//     * [x] String
//     * [x] JsonNumber
//   * [ ] properties
//     * [ ] Nullable
//   * [ ] geometry
//     * [ ] Nullable
//   * [ ] BBox
// * FeatureCollection
//   * [ ] Foreign Members
//   * [ ] BBox
//   * [x] Features
