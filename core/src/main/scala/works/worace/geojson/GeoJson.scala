package works.worace.geojson

import io.circe._
import io.circe.parser._
import io.circe.syntax._
import CoordinateCodec.implicits._

object GeoJson extends Codable[GeoJson] {
  val decoder = GeoJsonCodec.decoder
  val encoder = GeoJsonCodec.encoder
  val coreKeys =
    Set("type", "geometry", "coordinates", "properties", "features", "geometries", "id", "bbox")
}

trait ForeignMembers[A] {
  def withForeignMembers(foreignMembers: Option[JsonObject]): A
}

sealed trait Geometry extends GeoJson with ForeignMembers[Geometry]

/**
  * Core GeoJSON ADT, implemented as a sealed trait.
  *
  * Members of this type represent the individual GeoJSON types:
  *
  *  - [[FeatureCollection]]
  *  - [[Feature]]
  *  - [[Point]]
  *  - [[LineString]]
  *  - [[Polygon]]
  *  - [[MultiPoint]]
  *  - [[MultiLineString]]
  *  - [[MultiPolygon]]
  *  - [[GeometryCollection]]
  *
  * Additionally, a second trait ADT, [[Geometry]], contains the 7
  * geometric subtypes.
  *
  * Companion objects for GeoJson subtypes also implement the [[Codable]] trait,
  * which provides the interface for decoding GeoJson types from strings or [[https://circe.github.io/circe/api/io/circe/Json.html io.circe.Json]].
  */
sealed trait GeoJson {
  val foreignMembers: Option[JsonObject]
  val bbox: Option[BBox]
  def encode: Json = {
    this.asJson(GeoJsonCodec.encoder)
  }
  def `type`: String
  def asJsonObject: JsonObject = {
    addTypeKey(addBBox(addForeignMembers(baseJsonObject, foreignMembers), bbox))
  }

  protected def baseJsonObject: JsonObject

  private def addTypeKey(encoded: JsonObject): JsonObject = {
    encoded.add("type", Json.fromString(this.`type`))
  }

  private def addForeignMembers(encoded: JsonObject, fm: Option[JsonObject]): JsonObject = {
    fm.map { obj =>
        val foreignMembers = obj.filterKeys(!GeoJson.coreKeys.contains(_))
        encoded.deepMerge(foreignMembers)
      }
      .getOrElse(encoded)
  }

  private def addBBox(encoded: JsonObject, bbox: Option[BBox]): JsonObject = {
    import BBoxCodec.implicits._
    bbox.map { bbox => encoded.add("bbox", bbox.asJson) }.getOrElse(encoded)
  }
}

case class Point(
  coordinates: Coordinate,
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends Geometry {
  val `type` = "Point"
  def withForeignMembers(fm: Option[JsonObject]): Point = copy(foreignMembers = fm)
  def baseJsonObject: JsonObject = {
    JsonObject("coordinates" -> coordinates.asJson)
  }
}

case class LineString(
  coordinates: Vector[Coordinate],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends Geometry {
  val `type` = "LineString"
  def withForeignMembers(fm: Option[JsonObject]): LineString = copy(foreignMembers = fm)
  def baseJsonObject: JsonObject = JsonObject("coordinates" -> coordinates.asJson)

}

case class Polygon(
  coordinates: Vector[Vector[Coordinate]],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends Geometry {
  val `type` = "Polygon"
  def withForeignMembers(fm: Option[JsonObject]): Polygon = copy(foreignMembers = fm)
  def baseJsonObject: JsonObject = JsonObject("coordinates" -> coordinates.asJson)
}

case class MultiPoint(
  coordinates: Vector[Coordinate],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends Geometry {
  val `type` = "MultiPoint"
  def withForeignMembers(fm: Option[JsonObject]): MultiPoint = copy(foreignMembers = fm)
  def baseJsonObject: JsonObject = JsonObject("coordinates" -> coordinates.asJson)
}

case class MultiLineString(
  coordinates: Vector[Vector[Coordinate]],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends Geometry {
  val `type` = "MultiLineString"
  def withForeignMembers(fm: Option[JsonObject]): MultiLineString = copy(foreignMembers = fm)
  def baseJsonObject: JsonObject = JsonObject("coordinates" -> coordinates.asJson)
}

case class MultiPolygon(
  coordinates: Vector[Vector[Vector[Coordinate]]],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends Geometry {
  val `type` = "MultiPolygon"
  def withForeignMembers(fm: Option[JsonObject]): MultiPolygon = copy(foreignMembers = fm)
  def baseJsonObject: JsonObject = JsonObject("coordinates" -> coordinates.asJson)
}

case class GeometryCollection(
  geometries: Vector[Geometry],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends Geometry {
  val `type` = "GeometryCollection"
  def withForeignMembers(fm: Option[JsonObject]): GeometryCollection = copy(foreignMembers = fm)
  def baseJsonObject: JsonObject = {
    val children = geometries.map(child => Json.fromJsonObject(child.asJsonObject))
    JsonObject("geometries" -> children.asJson)
  }
}

case class Feature(
  id: Option[Either[JsonNumber, String]],
  properties: Option[JsonObject],
  geometry: Option[Geometry],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends GeoJson
    with ForeignMembers[Feature] {
  val `type` = "Feature"
  def withForeignMembers(fm: Option[JsonObject]): Feature = copy(foreignMembers = fm)
  def simpleId: Option[String] = id.map(_.fold(num => num.toString, s => s))
  def simpleProps: JsonObject = {
    val props = properties.getOrElse(JsonObject.empty)
    val fm = foreignMembers.getOrElse(JsonObject.empty)
    fm.deepMerge(props)
  }
  def simple: Option[SimpleFeature] = {
    geometry.map(geom => SimpleFeature(simpleId, simpleProps, geom))
  }

  def baseJsonObject: JsonObject = {
    import IdCodec.implicits._
    val base = JsonObject("geometry" -> geometry.map(_.asJsonObject.asJson).getOrElse(Json.Null))
    List(
      id.map(id => ("id", id.asJson)),
      properties.map(p => ("properties", p.asJson))
    ).flatten.foldLeft(base)((feature: JsonObject, pair: (String, Json)) =>
      feature.add(pair._1, pair._2)
    )
  }
}

case class FeatureCollection(
  features: Vector[Feature],
  bbox: Option[BBox],
  foreignMembers: Option[JsonObject]
) extends GeoJson
    with ForeignMembers[FeatureCollection] {
  val `type` = "FeatureCollection"
  def withForeignMembers(fm: Option[JsonObject]): FeatureCollection = copy(foreignMembers = fm)
  def simple: SimpleFeatureCollection = SimpleFeatureCollection(features.flatMap(_.simple))
  def baseJsonObject: JsonObject = JsonObject("features" -> features.map(_.asJsonObject).asJson)
}

object Point extends Codable[Point] {
  val decoder = PointCodec.decoder
  val encoder = PointCodec.encoder
  def apply(coord: Coordinate): Point = Point(coord, None, None)
  def apply(x: Double, y: Double): Point = Point(Coordinate(x, y))
}

object LineString extends Codable[LineString] {
  val decoder = LineStringCodec.decoder
  val encoder = LineStringCodec.encoder
  def apply(coords: Seq[Coordinate]): LineString = LineString(coords.toVector, None, None)
}

object Polygon extends Codable[Polygon] {
  val decoder = PolygonCodec.decoder
  val encoder = PolygonCodec.encoder
  def apply(coords: Seq[Seq[Coordinate]]): Polygon =
    Polygon(coords.map(_.toVector).toVector, None, None)
}

object MultiPoint extends Codable[MultiPoint] {
  val decoder = MultiPointCodec.decoder
  val encoder = MultiPointCodec.encoder
  def apply(coords: Seq[Coordinate]): MultiPoint = MultiPoint(coords.toVector, None, None)
}

object MultiLineString extends Codable[MultiLineString] {
  val decoder = MultiLineStringCodec.decoder
  val encoder = MultiLineStringCodec.encoder
  def apply(coords: Seq[Seq[Coordinate]]): MultiLineString =
    MultiLineString(coords.map(_.toVector).toVector, None, None)
}

object MultiPolygon extends Codable[MultiPolygon] {
  val decoder = MultiPolygonCodec.decoder
  val encoder = MultiPolygonCodec.encoder
  def apply(coords: Seq[Seq[Seq[Coordinate]]]): MultiPolygon =
    MultiPolygon(coords.map(_.map(_.toVector).toVector).toVector, None, None)
}

object GeometryCollection extends Codable[GeometryCollection] {
  val decoder = GeometryCollectionCodec.decoder
  val encoder = GeometryCollectionCodec.encoder
  def apply(geometries: Seq[Geometry]): GeometryCollection =
    GeometryCollection(geometries.toVector, None, None)
}

case class SimpleFeature(id: Option[String], properties: JsonObject, geometry: Geometry) {
  def propsAs[T](implicit decoder: Decoder[T]): Decoder.Result[TypedFeature[T]] = {
    Json.fromJsonObject(properties).as[T].map(props => TypedFeature(id, props, geometry))
  }
  def feature: Feature = {
    Feature(
      id = id.map(Right(_)),
      properties = Some(properties),
      geometry = Some(geometry),
      bbox = None,
      foreignMembers = None
    )
  }
}

case class SimpleFeatureCollection(features: Vector[SimpleFeature])
case class TypedFeature[T](id: Option[String], properties: T, geometry: Geometry)

object Feature extends Codable[Feature] {
  def empty: Feature = Feature(None, None, None, None, None)
  def apply(geometry: Geometry): Feature = Feature(None, None, Some(geometry), None, None)
  def apply(properties: JsonObject, geometry: Geometry): Feature =
    Feature(None, Some(properties), Some(geometry), None, None)
  def apply(id: String, properties: JsonObject, geometry: Geometry): Feature =
    Feature(Some(Right(id)), Some(properties), Some(geometry), None, None)
  def apply(id: Int, properties: JsonObject, geometry: Geometry): Feature =
    Feature(Some(Left(Json.fromInt(id).asNumber.get)), Some(properties), Some(geometry), None, None)
  val decoder = FeatureCodec.decoder
  val encoder = FeatureCodec.encoder
}

object FeatureCollection extends Codable[FeatureCollection] {
  def apply(features: Seq[Feature]): FeatureCollection =
    FeatureCollection(features.toVector, None, None)
  val decoder = FeatureCollectionCodec.decoder
  val encoder = FeatureCollectionCodec.encoder
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
// * [x] Point
// * [x] LineString
// * [x] Polygon
// * [x] MultiPoint
// * [x] MultiLineString
// * [x] MultiPolygon
// * [x] GeometryCollection
// Basic Geometry BBox
// * [x] Point
// * [x] LineString
// * [x] Polygon
// * [x] MultiPoint
// * [x] MultiLineString
// * [x] MultiPolygon
// * [x] GeometryCollection
// Basic Geometry Foreign Members
// * [x] Point
// * [x] LineString
// * [x] Polygon
// * [x] MultiPoint
// * [x] MultiLineString
// * [x] MultiPolygon
// * [x] GeometryCollection
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
// * [x] SimpleFeature typed conversions
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

// Test Permutations
// Geometries
// - Each geom type
// Bbox / no bbox
// - XY/XYZ/XYZM
// - Foreign Members / no foreign members
// Feature
// bbox / no bbox
// no id / string id / int id
// properties / no properties
// foreign members / no foreign member
// geometry / no geometry
// FeatureCollection
// 0 to N features
// bbox / no bbox
// foreign members / no foreign members

// JsonObjectGenerator (optional JSON obj excluding keywords)
// ForeignMembersGenerator (json obj)
// BBoxGenerator -> Non, XY, XYZ
// PropertiesGenerator (json obj)
// Point/LS/Poly/MP/MLS/MPoly generators
// GeomCollection generator( 1 to N base geoms repeated )
// Geometry generator (1 of geom generators + BBox + Foreign Members)
// FeatureIDGenerator
// Feature Generator ( geometry generator + bbox + props + foreign members + Id)
// FCGenerator ( foreign members + 1 to N Features repeated )
