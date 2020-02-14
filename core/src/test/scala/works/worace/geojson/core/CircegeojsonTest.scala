package works.worace.geojson.core

import org.scalatest.FeatureSpec

class CircegeojsonTest extends FeatureSpec {
  feature("example") {
    scenario("hello world") {
      assert(Circegeojson.hello == "World")
    }
  }
}
