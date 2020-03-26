package works.worace.geojson.core

import org.scalatest.FeatureSpec
import io.circe.{Json, JsonObject, Decoder, DecodingFailure}

class SimpleFeatureSpec extends FeatureSpec {
  import scala.language.implicitConversions
  implicit def stringToJson(s: String) = Json.fromString(s)
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
      val f = Feature(123, JsonObject("a" -> Json.fromString("b")), Point(1.0, 2.0))
      assert(f.simple.get.id == Some("123"))
    }

    scenario("Empty ID") {
      val f = Feature(
        None,
        None,
        Some(Point(1.0, 2.0)),
        None,
        None
      )
      assert(f.simple.get.id == None)
    }

    scenario("Merging foreign members") {
      val f = Feature(
        None,
        Some(JsonObject("a" -> "b")),
        Some(Point(1.0, 2.0)),
        None,
        Some(JsonObject("c" -> "d"))
      )
      assert(f.simple.map(_.properties) == Some(JsonObject("a" -> "b", "c" -> "d")))
    }

    scenario("Null properties coerced to default") {
      val f = Feature(
        None,
        None,
        Some(Point(1.0, 2.0)),
        None,
        None
      )
      assert(f.simple.map(_.properties) == Some(JsonObject.empty))
    }
  }

  feature("Simple FeatureCollection Interface") {
    scenario("converts all features to simple") {
      val f = Feature("id", JsonObject("a" -> Json.fromString("b")), Point(1.0, 2.0))
      val fc = FeatureCollection(List(Feature.empty, f))
      assert(fc.simple.features.size == 1)
      assert(fc.simple.features == List(f.simple.get))

    }
  }
}
