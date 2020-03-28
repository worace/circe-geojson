package works.worace.geojson.jts

import works.worace.geojson.core._
import org.locationtech.jts.geom.{
  Coordinate => JtsCoord, Geometry => JtsGeometry, GeometryFactory, PrecisionModel,
  Point => JtsPoint, Polygon => JtsPolygon, LinearRing
}

object Conversions {
  val SRID = 4326
  val pm = new PrecisionModel(PrecisionModel.FLOATING)
  val factory = new GeometryFactory(pm, SRID)

  object implicits {
    implicit class CoordToJts(val coord: Coordinate) extends AnyVal {
      def toJts: JtsCoord = {
        coord match {
          case Coordinate(x,y,None, None) => new JtsCoord(x,y)
          case Coordinate(x,y,Some(z), None) => new JtsCoord(x,y,z)
          case Coordinate(x,y,Some(z), Some(m)) => {
            val c = new JtsCoord(x,y,z)
            c.setM(m)
            c
          }
          case Coordinate(x,y,None, Some(m)) => throw new IllegalArgumentException("Can't supply M coordinate without z")
        }
      }
    }

    def coordArray(coords: Vector[Coordinate]): Array[JtsCoord] = {
      coords.map(_.toJts).toArray
    }

    def polygon(polygon: Polygon): JtsPolygon = {
      if (polygon.coordinates.isEmpty) {
        factory.createPolygon()
      } else {
        val Vector(outer, inner @ _*) = polygon.coordinates
        val shell = factory.createLinearRing(coordArray(polygon.coordinates.head))
        val interiors: Array[LinearRing] = polygon.coordinates.tail
          .map(coordArray)
          .map(coords => factory.createLinearRing(coords))
          .toArray
        factory.createPolygon(shell, interiors)
      }
    }

    implicit class ToJts(val geom: Geometry) extends AnyVal {
      def toJts: JtsGeometry = {
        geom match {
          case p: Point => factory.createPoint(p.coordinates.toJts)
          case ls: LineString => factory.createLineString(coordArray(ls.coordinates))
          case p: Polygon => polygon(p)
          case _ => throw new RuntimeException("...")
        }
      }
    }

  }
}
