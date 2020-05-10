package works.worace.geojson.jts

import works.worace.geojson
import org.locationtech.jts.geom._

class ConversionsSpec extends munit.FunSuite {
  import works.worace.geojson.TestData._
  import Conversions.implicits._

  def compareLineString(coords: Vector[geojson.Coordinate], jts: LineString) = {
    assert(jts.getCoordinates().size == coords.size)
    coords.zipWithIndex.foreach {
      case (c, i) =>
        compareCoord(c, jts.getCoordinateN(i))
    }
  }

  def comparePolygon(coords: Vector[Vector[geojson.Coordinate]], jts: Polygon) = {
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

  def compareCoord(coord: geojson.Coordinate, jts: Coordinate) = {
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

  def compareMultiLineString(gj: Vector[Vector[geojson.Coordinate]], jts: MultiLineString) = {
    gj.zipWithIndex.foreach {
      case (coords, idx) =>
        jts.getGeometryN(idx) match {
          case ls: LineString => compareLineString(coords, ls)
          case other          => fail("JTS MultiLineString should contain LineString geoms", clues(other))
        }
    }
  }

  def roundTrip(geom: geojson.Geometry): Unit = {
    assertEquals(geom, geom.toJts.toGeoJson)
  }

  test("Point") {
    val p = pointXY
    val j = p.toJts
    assert(j.isInstanceOf[Point])
    compareCoord(p.coordinates, j.asInstanceOf[Point].getCoordinate())
    roundTrip(p)
  }

  test("linestring") {
    val ls = linestringXY
    ls.toJts match {
      case geom: LineString => {
        compareLineString(ls.coordinates, geom)
      }
      case other => fail("should convert to jts linestring", clues(other))
    }
    roundTrip(ls)
  }

  test("polygon") {
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
      case other => fail("should convert to jts polygon", clues(other))
    }
    roundTrip(p)
  }

  test("multi point") {
    val mp = geojson.MultiPoint(coordSeqXY.take(3))
    mp.toJts match {
      case geom: MultiPoint => {
        assert(geom.getNumPoints() == 3)
        mp.coordinates.zip(geom.getCoordinates()).foreach {
          case (coord, jts) =>
            compareCoord(coord, jts)
        }
      }
      case other => fail("should convert to jts MultiPoint", clues(other))
    }
    roundTrip(mp)
  }

  test("MultiLineString") {
    val mls = multiLineStringXY
    mls.toJts match {
      case geom: MultiLineString => {
        assert(geom.getNumGeometries() == mls.coordinates.size)
        compareMultiLineString(mls.coordinates, geom)
      }
      case _ => fail("should convert to jts MultiLineString")
    }
    roundTrip(mls)
  }

  test("MultiPolygon") {
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
    roundTrip(mp)
  }

  test("GeometryCollection") {
    val geoms = Vector(
      pointXY,
      polygonXY(),
      multiLineStringXY
    )
    val gc = geojson.GeometryCollection(geoms)
    gc.toJts match {
      case geom: GeometryCollection => {
        assert(geom.getNumGeometries() == 3)

        (geoms(0), geom.getGeometryN(0)) match {
          case (gj: geojson.Point, jts: Point) => compareCoord(gj.coordinates, jts.getCoordinate)
          case _                               => fail("expecting 2 points to compare")
        }

        (geoms(1), geom.getGeometryN(1)) match {
          case (gj: geojson.Polygon, jts: Polygon) => comparePolygon(gj.coordinates, jts)
          case _                                   => fail("expecting 2 polygons to compare")
        }

        (geoms(2), geom.getGeometryN(2)) match {
          case (gj: geojson.MultiLineString, jts: MultiLineString) =>
            compareMultiLineString(gj.coordinates, jts)
          case _ => fail("expecting 2 multilinestrings to compare")
        }
      }
      case _ => fail("should convert to jts GeometryCollection")
    }
    roundTrip(gc)
  }
}
