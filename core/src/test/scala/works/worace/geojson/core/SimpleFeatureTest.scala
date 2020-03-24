package works.worace.geojson.core

import org.scalatest.FeatureSpec
import io.circe.{Json, JsonObject, Decoder, DecodingFailure}

class SimpleFeatureSpec extends FeatureSpec {
  import TestData._
  feature("Simple Feature Interface") {
    scenario("Basic") {
      val f = Feature("id", JsonObject("a" -> Json.fromString("b")), Point(1.0, 2.0))
      val exp = SimpleFeature(Some("id"), JsonObject("a" -> Json.fromString("b")), Point(1.0, 2.0))
      assert(f.simple == Some(exp))
    }

    scenario("Null geom") {
      assert(Feature.empty.simple.isEmpty)
      assert(Feature(Some(Right("test")), Some(JsonObject.empty), None, None, None).simple.isEmpty)
    }

    scenario("Num ID to string") {
    }

    scenario("Merging foreign members") {
    }

    scenario("Null properties coerced to default") {
    }
  }

  feature("Simple FeatureCollection Interface") {
  }
}
