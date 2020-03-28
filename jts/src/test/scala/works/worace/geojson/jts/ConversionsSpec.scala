package works.worace.geojson.jts

import org.scalatest.FeatureSpec
import works.worace.geojson.core
import org.locationtech.jts.geom._

class ConversionsSpec extends FeatureSpec {
  import works.worace.geojson.core.TestData._
  import Conversions.implicits._

  feature("Converting geometries") {
    scenario("Point") {
      val p = pointXY
      val j = p.toJts
      assert(j.isInstanceOf[Point])
      assert(p.coordinates.x == j.asInstanceOf[Point].getX)
      assert(p.coordinates.y == j.asInstanceOf[Point].getY)
      assert(j.getCoordinate.getZ.isNaN)
      assert(j.getCoordinate.getM.isNaN)
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

    def compareLineString(coords: Vector[core.Coordinate], jts: LineString) = {
      assert(jts.getCoordinates().size == coords.size)
      coords.zipWithIndex.foreach { case (c, i) =>
        assert(c.x == jts.getCoordinateN(i).getX)
        assert(c.y == jts.getCoordinateN(i).getY)
        assert(jts.getCoordinateN(i).getZ.isNaN())
        assert(jts.getCoordinateN(i).getM.isNaN())
      }
    }

    scenario("polygon") {
      val numRings = randInt(5)
      val p = polygonXY(numRings)
      p.toJts match {
        case geom: Polygon => {
          assert(geom.getNumInteriorRing == numRings - 1)
          p.coordinates.zipWithIndex.foreach { case (c, i) =>
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
  }
}
