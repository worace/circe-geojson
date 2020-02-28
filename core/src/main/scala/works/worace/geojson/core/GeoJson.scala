package works.worace.geojson.core

import io.circe._
// import io.circe.generic.auto._
import io.circe.parser._
// import io.circe.syntax._

object GeoJson {
  import io.circe.generic.extras.auto._
  import io.circe.generic.extras.Configuration
  implicit val geojsonTypeDiscriminator: Configuration =
    Configuration.default.withDiscriminator("type")
  import CoordinateSerde._

  def parse(rawJson: String): Either[io.circe.Error, GeoJson] = {
    // println(rawJson)
    // io.circe.parser.parse(rawJson).map { json =>
    //   println("parsed:")
    //   println(json)
    //   json.as[GeoJson]
    // }

    decode[GeoJson](rawJson)
  }
}

case class Coordinate(x: Double, y: Double, z: Option[Double], m: Option[Double]) {
  def arr: Array[Double] = {
    this match {
      case Coordinate(x, y, None, None) => Array(x, y)
      case Coordinate(x, y, Some(z), None) => Array(x, y, z)
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
    coord => Json.arr(coord.arr.flatMap(Json.fromDouble):_*)
  }

  implicit val decodeCoord: Decoder[Coordinate] = new Decoder[Coordinate] {
    final def apply(c: HCursor): Decoder.Result[Coordinate] = {
      c.as[Array[Double]]
        .filterOrElse(
          coords => coords.size > 1 && coords.size < 5,
          DecodingFailure("Invalid GeoJson Coordinates", c.history)
        )
        .map {
          case Array(x,y,z,m) => Coordinate(x, y, Some(z), Some(m))
          case Array(x,y,z) => Coordinate(x, y, Some(z), None)
          case Array(x,y) => Coordinate(x, y, None, None)
        }
    }
  }
}

// Q: Separate ADT for decoders / formats vs. public interface?
// MultiPoint(Seq[Point])
// vs MultiPoint(type: String, coordinates: Seq[Coordinate])

sealed trait Geometry

sealed trait GeoJson
case class Point(
  `type`: String,
  coordinates: Coordinate
) extends GeoJson with Geometry
case class LineString(
  `type`: String,
  coordinates: Vector[Coordinate]
) extends GeoJson with Geometry
case class Polygon(
  `type`: String,
  coordinates: Vector[Vector[Coordinate]]
) extends GeoJson with Geometry
case class MultiPoint(
  `type`: String,
  coordinates: Vector[Coordinate]
) extends GeoJson with Geometry
case class MultiLineString(
  `type`: String,
  coordinates: Vector[Vector[Coordinate]]
) extends GeoJson with Geometry
case class MultiPolygon(
  `type`: String,
  coordinates: Vector[Vector[Vector[Coordinate]]]
) extends GeoJson with Geometry
case class GeometryCollection(
  `type`: String,
  geometries: Vector[Geometry]
) extends GeoJson with Geometry

// case class Geometry(
//   `type`: String,
//   coordinates: Array[Double]
// ) extends GeoJson


object Point {
  def apply(coord: Coordinate): Point = Point("Point", coord)
  def apply(x: Double, y: Double): Point = Point(Coordinate(x, y))
}

object LineString {
  def apply(coords: Seq[Coordinate]): LineString = LineString("LineString", coords.toVector)
}

object Polygon {
  def apply(coords: Seq[Seq[Coordinate]]): Polygon = Polygon("Polygon", coords.map(_.toVector).toVector)
}

object MultiPoint {
  def apply(coords: Seq[Coordinate]): MultiPoint = MultiPoint("MultiPoint", coords.toVector)
}

object MultiLineString {
  def apply(coords: Seq[Seq[Coordinate]]): MultiLineString = MultiLineString("MultiLineString", coords.map(_.toVector).toVector)
}

object MultiPolygon {
  def apply(coords: Seq[Seq[Seq[Coordinate]]]): MultiPolygon = MultiPolygon("MultiPolygon", coords.map(_.map(_.toVector).toVector).toVector)
}

object GeometryCollection {
  def apply(geometries: Seq[Geometry]): GeometryCollection = GeometryCollection("GeometryCollection", geometries.toVector)
}
