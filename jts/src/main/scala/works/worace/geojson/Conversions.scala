package works.worace.geojson.jts

import works.worace.geojson._
import org.locationtech.jts.{geom => jts}
import org.locationtech.jts.geom.{GeometryFactory, PrecisionModel}
import org.locationtech.jts.geom.CoordinateSequence

object Conversions {
  protected val SRID = 4326
  protected val pm = new PrecisionModel(PrecisionModel.FLOATING)
  private val factory = new GeometryFactory(pm, SRID)
  private def coordArray(coords: Vector[Coordinate]): Array[jts.Coordinate] = {
    coords.map(coordinate).toArray
  }

  private def polygonFromCoordRings(rings: Vector[Vector[Coordinate]]): jts.Polygon = {
    val Vector(outer, inner @ _*) = rings
    val shell = factory.createLinearRing(coordArray(outer))
    val interiors: Array[jts.LinearRing] = inner
      .map(coordArray)
      .map(coords => factory.createLinearRing(coords))
      .toArray
    factory.createPolygon(shell, interiors)
  }

  private def polygon(polygon: Polygon): jts.Polygon = {
    if (polygon.coordinates.isEmpty) {
      factory.createPolygon()
    } else {
      polygonFromCoordRings(polygon.coordinates)
    }
  }

  private def multiPoint(mp: MultiPoint): jts.MultiPoint = {
    factory.createMultiPointFromCoords(mp.coordinates.map(coordinate).toArray)
  }

  private def multiLineString(mls: MultiLineString): jts.MultiLineString = {
    val coordSeqs = mls.coordinates.map(coordArray(_))
    val lines = coordSeqs.map(c => factory.createLineString(c)).toArray
    factory.createMultiLineString(lines)
  }

  private def multiPolygon(mp: MultiPolygon): jts.MultiPolygon = {
    val polys = mp.coordinates.map(polygonFromCoordRings)
    factory.createMultiPolygon(polys.toArray)
  }

  private def coordinate(coord: Coordinate): jts.Coordinate = {
    coord match {
      case Coordinate(x, y, None, None)    => new jts.Coordinate(x, y)
      case Coordinate(x, y, Some(z), None) => new jts.Coordinate(x, y, z)
      case Coordinate(x, y, Some(z), Some(m)) => {
        val c = new jts.Coordinate(x, y, z)
        c.setM(m)
        c
      }
      case Coordinate(x, y, None, Some(m)) =>
        throw new IllegalArgumentException("Can't supply M coordinate without z")
    }
  }

  def geometryCollection(gc: GeometryCollection): jts.GeometryCollection = {
    val children = gc.geometries.map(toJts)
    factory.createGeometryCollection(children.toArray)
  }

  def toJts(geom: Geometry): jts.Geometry = {
    geom match {
      case g: Point              => factory.createPoint(coordinate(g.coordinates))
      case g: LineString         => factory.createLineString(coordArray(g.coordinates))
      case g: Polygon            => polygon(g)
      case g: MultiPoint         => multiPoint(g)
      case g: MultiLineString    => multiLineString(g)
      case g: MultiPolygon       => multiPolygon(g)
      case g: GeometryCollection => geometryCollection(g)
    }
  }

  private def coord(coord: jts.Coordinate): Coordinate = {
    Coordinate(
      coord.getX(),
      coord.getY(),
      Option(coord.getZ).filterNot(_.isNaN()),
      Option(coord.getM).filterNot(_.isNaN())
    )
  }

  private def coordSeq(coordArray: Array[jts.Coordinate]): Vector[Coordinate] = {
    coordArray.toVector.map(coord(_))
  }

  private def polygonRings(p: jts.Polygon): Vector[Vector[Coordinate]] = {
    val ext = Option(p.getExteriorRing).map(ls => coordSeq(ls.getCoordinates())).toVector
    val interiors = (0 until p.getNumInteriorRing()).toVector.map { n =>
      coordSeq(p.getInteriorRingN(n).getCoordinates)
    }
    ext ++ interiors
  }

  private def multiPointCoords(mp: jts.MultiPoint): Vector[Coordinate] = {
    mp.getCoordinates.toVector.map(coord(_))
  }

  private def multiLineStringCoords(mls: jts.MultiLineString): Vector[Vector[Coordinate]] = {
    (0 until mls.getNumGeometries()).toVector.map { n =>
      coordSeq(mls.getGeometryN(n).getCoordinates())
    }
  }

  private def multiPolygonCoords(mp: jts.MultiPolygon): Vector[Vector[Vector[Coordinate]]] = {
    (0 until mp.getNumGeometries()).toVector.map { n =>
      polygonRings(mp.getGeometryN(n).asInstanceOf[jts.Polygon])
    }
  }

  /** Convert a JTS Geometry to a GeoJson Geometry
    *
    * Note that only valid GeoJSON types are supported:
    * Point, LineString, Polygon, MultiPoint, MultiLineString, MultiPolygon,
    * and GeometryCollections containing these types.
    *
    * @param geom JTS Geometry to convert
    */
  def fromJts(geom: jts.Geometry): Geometry = {
    geom match {
      case g: jts.Point           => Point(coord(g.getCoordinate()))
      case g: jts.LineString      => LineString(coordSeq(g.getCoordinates()))
      case g: jts.Polygon         => Polygon(polygonRings(g))
      case g: jts.MultiPoint      => MultiPoint(multiPointCoords(g))
      case g: jts.MultiLineString => MultiLineString(multiLineStringCoords(g))
      case g: jts.MultiPolygon    => MultiPolygon(multiPolygonCoords(g))
      case g: jts.GeometryCollection =>
        GeometryCollection(
          (0 until g.getNumGeometries()).map(n => g.getGeometryN(n)).map(fromJts(_))
        )
    }
  }

  object implicits {
    implicit class JtsToGeometry(val geom: jts.Geometry) extends AnyVal {
      def toGeoJson: Geometry = Conversions.fromJts(geom)
    }
    implicit class GeometryToJts(val geom: Geometry) extends AnyVal {
      def toJts: jts.Geometry = Conversions.toJts(geom)
    }
    implicit class FeatureToJts(val f: Feature) extends AnyVal {
      def toJts: Option[JtsFeature] = {
        f.simple.map { sf => JtsFeature(sf.id, sf.properties, Conversions.toJts(sf.geometry)) }
      }
    }
  }
}
