package works.worace.geojson.core

import io.circe.{Json, JsonObject, Decoder, DecodingFailure}
import io.circe.parser.decode
import scala.reflect.ClassTag

class GeometryCodecTest extends munit.FunSuite with TestHelpers {
  import TestData._
  import GeometryCodec.implicits._

  test("round-tripping base cases") {
    BaseGeomCases.all.foreach { c => codecCase[Geometry](c) }
  }
}
