package works.worace.geojson.jts

import org.scalatest.FeatureSpec
import works.worace.geojson.core
import org.locationtech.jts.geom._

class ConversionsSpec extends FeatureSpec {
  import works.worace.geojson.core.TestData._
  import Conversions.implicits._

  def compareLineString(coords: Vector[core.Coordinate], jts: LineString) = {
    assert(jts.getCoordinates().size == coords.size)
    coords.zipWithIndex.foreach {
      case (c, i) =>
        compareCoord(c, jts.getCoordinateN(i))
    }
  }

  def comparePolygon(coords: Vector[Vector[core.Coordinate]], jts: Polygon) = {
    coords.zipWithIndex.foreach {
      case (c, i) =>
        if (i == 0) {
          val ring: LineString = jts.getExteriorRing()
          compareLineString(c, ring)
        } else {
          val ring = jts.getInteriorRingN(i - 1)
          compareLineString(c, ring)
        }
    }
  }

  def compareCoord(coord: core.Coordinate, jts: Coordinate) = {
    assert(coord.x == jts.getX)
    assert(coord.y == jts.getY)
    coord.z match {
      case Some(z) => assert(jts.getZ == z)
      case None    => assert(jts.getZ.isNaN)
    }
    coord.m match {
      case Some(m) => assert(jts.getM == m)
      case None    => assert(jts.getM.isNaN)
    }
  }

  feature("Converting geometries") {
    scenario("Point") {
      val p = pointXY
      val j = p.toJts
      assert(j.isInstanceOf[Point])
      compareCoord(p.coordinates, j.asInstanceOf[Point].getCoordinate())
    }

    scenario("linestring") {
      val ls = linestringXY
      ls.toJts match {
        case geom: LineString => {
          compareLineString(ls.coordinates, geom)
        }
        case _ => fail("should convert to jts linestring")
      }
    }

    scenario("polygon") {
      val numRings = randInt(5)
      val p = polygonXY(numRings)
      p.toJts match {
        case geom: Polygon => {
          assert(geom.getNumInteriorRing == numRings - 1)
          p.coordinates.zipWithIndex.foreach {
            case (c, i) =>
              if (i == 0) {
                val ring: LineString = geom.getExteriorRing()
                compareLineString(c, ring)
              } else {
                val ring = geom.getInteriorRingN(i - 1)
                compareLineString(c, ring)
              }
          }
        }
        case _ => fail("should convert to jts polygon")
      }
    }

    scenario("multi point") {
      val mp = core.MultiPoint(coordSeqXY.take(3))
      mp.toJts match {
        case geom: MultiPoint => {
          assert(geom.getNumPoints() == 3)
          mp.coordinates.zip(geom.getCoordinates()).foreach {
            case (coord, jts) =>
              compareCoord(coord, jts)
          }
        }
        case _ => fail("should convert to jts polygon")
      }
    }

    scenario("MultiLineString") {
      val mls = multiLineStringXY
      mls.toJts match {
        case geom: MultiLineString => {
          assert(geom.getNumGeometries() == mls.coordinates.size)
          mls.coordinates.zipWithIndex.foreach {
            case (coords, idx) =>
              geom.getGeometryN(idx) match {
                case ls: LineString => compareLineString(coords, ls)
                case _              => fail("JTS MultiLineString should contain LineString geoms")
              }
          }
        }
        case _ => fail("should convert to jts MultiLineString")
      }
    }

    scenario("MultiPolygon") {
      val mp = multiPolygonXY
      mp.toJts match {
        case geom: MultiPolygon => {
          assert(geom.getNumGeometries() == mp.coordinates.size)
          mp.coordinates.zipWithIndex.foreach {
            case (polygonRings, idx) =>
              geom.getGeometryN(idx) match {
                case p: Polygon => comparePolygon(polygonRings, p)
                case _          => fail("JTS MultiPolygon should contain Polygons")
              }
          }
        }
        case _ => fail("should convert to jts MultiPolygon")
      }
    }
  }
}
