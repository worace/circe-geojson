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

  def fromJson(json: Json): Either[io.circe.Error, GeoJson] = {
    json.as[GeoJson](GeoJsonSerde.decoder)
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
        case Array(x1, y1, z1, x2, y2, z2) =>
          Right(BBox(Coordinate(x1, y1, z1), Coordinate(x2, y2, z2)))
        case _ => Left(DecodingFailure("Invalid GeoJson BBox", c.history))
      }
    }
  }
}

object GeoJsonSerde {
  import io.circe.syntax._
  import works.worace.geojson.core.codecs.{Geometry => GeometryCodec}
  implicit val geomEncoder: Encoder[Geometry] = GeometryCodec.encoder
  implicit val geomDecoder: Decoder[Geometry] = GeometryCodec.decoder

  val featureBase = JsonObject("type" -> Json.fromString("Feature"))
  def feature(f: Feature): JsonObject = {
    import works.worace.geojson.core.codecs.Geometry._
    List(
      f.id.map(id => ("id", id.asJson(IdSerde.encodeEither))),
      f.properties.map(p => ("properties", p.asJson)),
      f.bbox.map(bb => ("bbox", bb.asJson(BBoxSerde.bboxEncoder))),
      f.geometry.map((g: Geometry) => ("geometry", g.asJson))
    ).flatten
      .foldLeft(featureBase)(
        (feature: JsonObject, pair: (String, Json)) => feature.add(pair._1, pair._2)
      )
  }

  val fcBase = JsonObject("type" -> Json.fromString("FeatureCollection"))
  def featureCollectionBase(fc: FeatureCollection): JsonObject = {
    import io.circe.syntax._
    val withFeatures = fcBase.add("features", fc.features.map(_.asJson(featureEncoder)).asJson)
    // val features = JsonObject("features" -> fc.features)
    fc.bbox match {
      case None       => withFeatures
      case Some(bbox) => withFeatures.add("bbox", bbox.asJson(BBoxSerde.bboxEncoder))
    }
  }

  def base(g: GeoJson): Json = {
    import io.circe.syntax._
    g match {
      case geom: Geometry        => geom.asJson
      case f: Feature            => feature(f).asJson
      case fc: FeatureCollection => featureCollectionBase(fc).asJson
    }
  }

  implicit val featureEncoder: Encoder[Feature] = Encoder.instance { gj =>
    import io.circe.syntax._
    feature(gj).asJson
  }

  implicit val encoder: Encoder[GeoJson] = Encoder.instance { gj =>
    import io.circe.syntax._
    base(gj).asJson
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

sealed trait Geometry extends GeoJson

sealed trait GeoJson {
  val foreignMembers: Option[JsonObject]
  val bbox: Option[BBox]
  def `type`: String
  def withForeignMembers(fm: JsonObject): GeoJson
  def encode: Json = {
    import GeoJsonSerde.encoder
    import io.circe.syntax._
    this.asJson
  }
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
  def simpleId: Option[String] = id.map(_.fold(num => num.toString, s => s))
  def simpleProps: JsonObject = {
    val props = properties.getOrElse(JsonObject.empty)
    val fm = foreignMembers.getOrElse(JsonObject.empty)
    fm.deepMerge(props)
  }
  def simple: Option[SimpleFeature] = {
    geometry.map(geom => SimpleFeature(simpleId, simpleProps, geom))
  }
}

case class FeatureCollection(
  features: Vector[Feature],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends GeoJson {
  val `type` = "FeatureCollection"
  def withForeignMembers(fm: JsonObject): GeoJson = copy(foreignMembers = Some(fm))
  def simple: SimpleFeatureCollection = SimpleFeatureCollection(features.flatMap(_.simple))
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

case class SimpleFeature(id: Option[String], properties: JsonObject, geometry: Geometry) {
  def propsAs[T](implicit decoder: Decoder[T]): Decoder.Result[TypedFeature[T]] = {
    Json.fromJsonObject(properties).as[T].map(props => TypedFeature(id, props, geometry))
  }
}

case class SimpleFeatureCollection(features: Vector[SimpleFeature])
case class TypedFeature[T](id: Option[String], properties: T, geometry: Geometry)

object Feature {
  def empty: Feature = Feature(None, None, None, None, None)
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
// * [x] Feature
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
// Misc
// * [ ] JSNumber overflow case
// * [ ] XYM Coordinate (no Z) encoding
// "Simple" interface
// * [x] Feature to SimpleFeature
//   * [x] Num ID
//   * [x] String ID
//   * [x] No ID
//   * [x] No geom (is None)
//   * [x] null props (filled with empty jsonobject)
//   * [x] foreign members and props (merged)
// * [x] FeatureCollection to SimpleFeatureCollection
// * [ ] SimpleFeature typed conversions
// JTS Conversions
// ToJts
// * [x] Point
// * [x] LineString
// * [x] Polygon
// * [x] MultiPoint
// * [x] MultiLineString
// * [x] MultiPolygon
// * [x] GeometryCollection
// FromJts
// * [ ] Point
// * [ ] LineString
// * [ ] Polygon
// * [ ] MultiPoint
// * [ ] MultiLineString
// * [ ] MultiPolygon
// * [ ] GeometryCollection
