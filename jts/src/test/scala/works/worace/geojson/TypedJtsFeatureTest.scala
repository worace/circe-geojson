package works.worace.geojson.jts

import io.circe._, io.circe.generic.semiauto._
import works.worace.geojson.{TestData, Feature, Point}

class TypedJtsFeatureSpec extends munit.FunSuite {
  import scala.language.implicitConversions
  implicit def stringToJson(s: String) = Json.fromString(s)
  import TestData._
  import Conversions.implicits.GeometryToJts
  import Conversions.implicits.FeatureToJts

  case class Props(a: String)
  implicit val decoder = deriveDecoder[Props]

  test("typing feature properties with circe decoder") {
    val f = Feature("id", JsonObject("a" -> "b"), Point(1.0, 2.0))
    val exp = TypedJtsFeature(Some("id"), Props("b"), Point(1.0, 2.0).toJts)
    assertEquals(f.toJts.map(_.typed[Props]), Some(Right(exp)))
  }
}
