package works.worace.geojson.jts

import org.scalatest.FeatureSpec
import org.locationtech.jts.geom.{Point, Coordinate}

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
  }
}
