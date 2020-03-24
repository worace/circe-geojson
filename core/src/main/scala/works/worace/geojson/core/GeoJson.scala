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
    decode[GeoJson](rawJson)(GeoJsonSerde.decoder)
  }
}

object IdSerde {
  type ID = Either[Long, String]

  // https://github.com/circe/circe/issues/672
  implicit def encodeEither[A, B](
    implicit
    encoderA: Encoder[A],
    encoderB: Encoder[B]
  ): Encoder[Either[A, B]] = {
    import io.circe.syntax._
    o: Either[A, B] => o.fold(_.asJson, _.asJson)
  }

  implicit def decodeEither[A, B](
    implicit
    decoderA: Decoder[A],
    decoderB: Decoder[B]
  ): Decoder[Either[A, B]] = decoderA.either(decoderB)
}

object CoordinateSerde {
  implicit val encodeCoord: Encoder[Coordinate] = Encoder.instance {
    // TODO - how does this handle large numbers that circe can't represent as JsonNumber
    coord =>
      Json.arr(coord.array.flatMap(Json.fromDouble): _*)
  }

  implicit val decodeCoord: Decoder[Coordinate] = new Decoder[Coordinate] {
    final def apply(c: HCursor): Decoder.Result[Coordinate] = {
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

object BBoxSerde {
  import io.circe.syntax._
  implicit val bboxEncoder: Encoder[BBox] = Encoder.instance { bbox =>
    bbox.flat.asJson
  }

  implicit val bboxDecoder: Decoder[BBox] = new Decoder[BBox] {
    final def apply(c: HCursor): Decoder.Result[BBox] = {
      import CoordinateSerde._
      c.as[Array[Double]].flatMap {
        case Array(x1, y1, x2, y2) => Right(BBox(Coordinate(x1, y1), Coordinate(x2, y2)))
        case Array(x1, y1, z1, x2, y2, z2) => Right(BBox(Coordinate(x1, y1, z1), Coordinate(x2, y2, z2)))
        case _ => Left(DecodingFailure("Invalid GeoJson BBox", c.history))
      }
    }
  }
}

object GeoJsonSerde {
  def geomCollectionBase(gc: GeometryCollection): JsonObject = {
    import io.circe.syntax._
    val children = gc.geometries.map(child => Json.fromJsonObject(geomBase(child)))
    JsonObject("geometries" -> children.asJson)
  }

  def geomBase(g: Geometry): JsonObject = {
    import io.circe.syntax._
    import CoordinateSerde._
    g match {
      case geom: Point              => JsonObject("coordinates" -> geom.coordinates.asJson)
      case geom: LineString         => JsonObject("coordinates" -> geom.coordinates.asJson)
      case geom: Polygon            => JsonObject("coordinates" -> geom.coordinates.asJson)
      case geom: MultiPoint         => JsonObject("coordinates" -> geom.coordinates.asJson)
      case geom: MultiLineString    => JsonObject("coordinates" -> geom.coordinates.asJson)
      case geom: MultiPolygon       => JsonObject("coordinates" -> geom.coordinates.asJson)
      case geom: GeometryCollection => geomCollectionBase(geom)
    }
  }

  def featureBase(f: Feature): JsonObject = {
    val geom = f.geometry match {
      case None       => JsonObject.empty
      case Some(geom) => JsonObject("geometry" -> Json.fromJsonObject(geomBase(geom)))
    }
    f.properties match {
      case None        => geom
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
      case geom: Geometry        => geomBase(geom)
      case f: Feature            => featureBase(f)
      case fc: FeatureCollection => featureCollectionBase(fc)
    }
  }

  implicit val encodeGeoJson: Encoder[GeoJson] = Encoder.instance { gj =>
    Json.fromJsonObject(base(gj).add("type", Json.fromString(gj.`type`)))
  }

  val coreKeys =
    Set("type", "geometry", "coordinates", "properties", "features", "geometries", "id", "bbox")

  object Base {
    import io.circe.generic.extras.auto._
    import io.circe.generic.extras.Configuration
    implicit val geojsonTypeDiscriminator: Configuration =
      Configuration.default.withDiscriminator("type")
    val decoder: Decoder[GeoJson] = new Decoder[GeoJson] {
      final def apply(c: HCursor): Decoder.Result[GeoJson] = {
        import CoordinateSerde._
        import IdSerde._
        import BBoxSerde._
        c.as[GeoJson]
      }
    }
  }

  val decoder: Decoder[GeoJson] = new Decoder[GeoJson] {
    final def apply(c: HCursor): Decoder.Result[GeoJson] = {
      c.as[JsonObject]
        .flatMap { obj =>
          Json
            .fromJsonObject(obj)
            .as[GeoJson](Base.decoder)
            .map { base =>
              val foreignMembers = obj.filterKeys(!coreKeys.contains(_))
              if (foreignMembers.nonEmpty) {
                base.withForeignMembers(foreignMembers)
              } else {
                base
              }
            }
        }
    }
  }
}

case class Coordinate(x: Double, y: Double, z: Option[Double], m: Option[Double]) {
  def array: Array[Double] = {
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

  def apply(x: Double, y: Double, z: Double, m: Double): Coordinate = {
    Coordinate(x, y, Some(z), Some(m))
  }
}

case class BBox(min: Coordinate, max: Coordinate) {
  def flat: Array[Double] = {
    min.array ++ max.array
  }
}

sealed trait Geometry

sealed trait GeoJson {
  val foreignMembers: Option[JsonObject]
  val bbox: Option[BBox]
  def `type`: String
  def withForeignMembers(fm: JsonObject): GeoJson
}

case class Point(
  coordinates: Coordinate,
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends GeoJson
    with Geometry {
  val `type` = "Point"
  def withForeignMembers(fm: JsonObject): GeoJson = copy(foreignMembers = Some(fm))
}
case class LineString(
  coordinates: Vector[Coordinate],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends GeoJson
    with Geometry {
  val `type` = "LineString"
  def withForeignMembers(fm: JsonObject): GeoJson = copy(foreignMembers = Some(fm))
}
case class Polygon(
  coordinates: Vector[Vector[Coordinate]],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends GeoJson
    with Geometry {
  val `type` = "Polygon"
  def withForeignMembers(fm: JsonObject): GeoJson = copy(foreignMembers = Some(fm))
}
case class MultiPoint(
  coordinates: Vector[Coordinate],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends GeoJson
    with Geometry {
  val `type` = "MultiPoint"
  def withForeignMembers(fm: JsonObject): GeoJson = copy(foreignMembers = Some(fm))
}
case class MultiLineString(
  coordinates: Vector[Vector[Coordinate]],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends GeoJson
    with Geometry {
  val `type` = "MultiLineString"
  def withForeignMembers(fm: JsonObject): GeoJson = copy(foreignMembers = Some(fm))
}
case class MultiPolygon(
  coordinates: Vector[Vector[Vector[Coordinate]]],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends GeoJson
    with Geometry {
  val `type` = "MultiPolygon"
  def withForeignMembers(fm: JsonObject): GeoJson = copy(foreignMembers = Some(fm))
}
case class GeometryCollection(
  geometries: Vector[Geometry],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends GeoJson
    with Geometry {
  val `type` = "GeometryCollection"
  def withForeignMembers(fm: JsonObject): GeoJson = copy(foreignMembers = Some(fm))
}
case class Feature(
  id: Option[Either[JsonNumber, String]],
  properties: Option[JsonObject],
  geometry: Option[Geometry],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends GeoJson {
  val `type` = "Feature"
  def withForeignMembers(fm: JsonObject): GeoJson = copy(foreignMembers = Some(fm))
}

case class FeatureCollection(
  features: Vector[Feature],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends GeoJson {
  val `type` = "FeatureCollection"
  def withForeignMembers(fm: JsonObject): GeoJson = copy(foreignMembers = Some(fm))
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
  def apply(geometry: Geometry): Feature = Feature(None, None, Some(geometry), None, None)
  def apply(properties: JsonObject, geometry: Geometry): Feature =
    Feature(None, Some(properties), Some(geometry), None, None)
  def apply(id: String, properties: JsonObject, geometry: Geometry): Feature =
    Feature(Some(Right(id)), Some(properties), Some(geometry), None, None)
  def apply(id: Int, properties: JsonObject, geometry: Geometry): Feature =
    Feature(Some(Left(Json.fromInt(id).asNumber.get)), Some(properties), Some(geometry), None, None)
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
// * [x] Test: fails with too few and too many dimensions
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
//   * [x] properties
//     * [x] Nullable
//     * [x] Omittable
//   * [x] geometry
//     * [x] Nullable
//     * [x] omittable
//   * [x] BBox
//   * [x] XYZ / XYZM geometries
// * FeatureCollection
//   * [x] Foreign Members
//   * [x] BBox
//   * [x] Features
// Basic Geometry XYZM
// * [ ] Point
// * [ ] LineString
// * [ ] Polygon
// * [ ] MultiPoint
// * [ ] MultiLineString
// * [ ] MultiPolygon
// * [ ] GeometryCollection
// Basic Geometry BBox
// * [ ] Point
// * [ ] LineString
// * [ ] Polygon
// * [ ] MultiPoint
// * [ ] MultiLineString
// * [ ] MultiPolygon
// * [ ] GeometryCollection
// Basic Geometry Foreign Members
// * [ ] Point
// * [ ] LineString
// * [ ] Polygon
// * [ ] MultiPoint
// * [ ] MultiLineString
// * [ ] MultiPolygon
// * [ ] GeometryCollection


// "Simple" interface
// JTS Conversions
