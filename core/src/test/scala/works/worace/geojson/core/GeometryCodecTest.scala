package works.worace.geojson.core

import io.circe.{Json, JsonObject, Decoder, DecodingFailure}
import io.circe.parser.decode
import scala.reflect.ClassTag

class GeometryCodecTest extends munit.FunSuite {
  import TestData._
  import GeometryCodec.implicits._

  def codecCase[G <: GeoJson: ClassTag](c: Case)(implicit decoder: Decoder[G]) = {
    c.decoded match {
      case g: G => {
        decode[G](c.encoded)
          .map((res: G) => assert(res == g))
          .getOrElse(fail("Parsing failure"))
      }
      case _ => fail("GeometryCases should only be run against Geometries")
    }
  }

  test("round-tripping base cases") {
    BaseGeomCases.all.foreach { c => codecCase[Geometry](c) }
  }
}
