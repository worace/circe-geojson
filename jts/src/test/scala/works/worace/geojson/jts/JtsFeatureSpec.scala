package works.worace.geojson.jts

import io.circe.{Json, JsonObject}
import works.worace.geojson.core.{TestData, Feature, Point}

class JtsFeatureSpec extends munit.FunSuite {
  import scala.language.implicitConversions
  implicit def stringToJson(s: String) = Json.fromString(s)
  import TestData._
  import Conversions.implicits.GeometryToJts
  import Conversions.implicits.FeatureToJts

  test("Basic") {
    val f = Feature("id", JsonObject("a" -> "b"), Point(1.0, 2.0))
    assertEquals(
      f.toJts,
      Some(
        JtsFeature(
          Some("id"),
          JsonObject("a" -> "b"),
          Point(1.0, 2.0).toJts
        )
      )
    )
  }

  test("Null Geom is empty") {
    assertEquals(Feature.empty.toJts, None)
  }

  test("numeric id") {
    val props = JsonObject("a" -> "b")
    val f = Feature(123, props, Point(1.0, 2.0))
    val jts = JtsFeature(Some("123"), props, Point(1.0, 2.0).toJts)
    assertEquals(f.toJts, Some(jts))
  }

  test("no id") {
    val f = Feature(None, None, Some(Point(1.0, 2.0)), None, None)
    val jts = JtsFeature(None, JsonObject(), Point(1.0, 2.0).toJts)
    assertEquals(f.toJts, Some(jts))
  }
}

// Feature Variants
// SimpleFeature
// TypedFeature
// JTS Feature
// Typed JTS Feature
