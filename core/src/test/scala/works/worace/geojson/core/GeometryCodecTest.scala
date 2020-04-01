package works.worace.geojson.core

import org.scalatest.FeatureSpec
import io.circe.{Json, JsonObject, Decoder, DecodingFailure}
import io.circe.parser.decode
import scala.reflect.ClassTag

class GeometryCodecTest extends FeatureSpec {
  import TestData._
  import GeometryCodec._

  def codecCase[G <: GeoJson : ClassTag](c: Case)(implicit decoder: Decoder[G]) = {
    c.decoded match {
      case g: G => {
        decode[G](c.encoded)
          .map((res: G) => assert(res == g))
          .getOrElse(fail("Parsing failure"))
      }
      case _ => fail("GeometryCases should only be run against Geometries")
    }
  }

  feature("Decoding geometries") {
    scenario("") {
      BaseGeomCases.all.foreach { c =>
        codecCase[Geometry](c)
      }
    }
  }
}
