package works.worace.geojson.core

import scala.reflect.ClassTag
import io.circe.parser.decode
import io.circe.{Json, JsonObject, Decoder, DecodingFailure}

trait TestHelpers extends munit.FunSuite {
  import TestData.Case
  import io.circe.syntax._
  import GeoJsonCodec.implicits._
  def roundTripCase(gj: GeoJson) {
    val encoded = gj.asJson
    GeoJson
      .fromJson(encoded)
      .fold(
        err => fail(s"Failed round-trip: ${gj}", clues(gj, err, encoded)),
        decoded => assertEquals(decoded, gj)
      )

  }

  def codecCase[G <: GeoJson: ClassTag](c: Case)(implicit decoder: Decoder[G]) = {
    c.decoded match {
      case g: G => {
        decode[G](c.encoded)
          .map((res: G) => assertEquals(res, g))
          .getOrElse(fail("Parsing failure"))
      }
      case _ => fail("GeometryCases should only be run against Geometries")
    }
  }
}
