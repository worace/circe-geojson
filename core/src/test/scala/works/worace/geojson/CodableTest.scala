package works.worace.geojson

import io.circe.{Json, JsonObject, Decoder, DecodingFailure}
import io.circe.parser.decode
import scala.reflect.ClassTag

class CodableTest extends munit.FunSuite with TestHelpers {
  import TestData._
  import GeometryCodec.implicits._

  test("per-geom codec permutation round trips") {
    codablePermutationTests(Permutations.points, Point)
    codablePermutationTests(Permutations.lineStrings, LineString)
    codablePermutationTests(Permutations.polygons, Polygon)
    codablePermutationTests(Permutations.multiPoints, MultiPoint)
    codablePermutationTests(Permutations.multiLineStrings, MultiLineString)
    codablePermutationTests(Permutations.multiPolygons, MultiPolygon)
    codablePermutationTests(Permutations.geometryCollections, GeometryCollection)
    codablePermutationTests(Permutations.features, Feature)
    codablePermutationTests(Permutations.featureCollections, FeatureCollection)
  }

  def codablePermutationTests[G <: GeoJson : ClassTag](permutations: Vector[G], codable: Codable[G]) = {
    permutations.foreach { gj =>
      val encoded = codable.asJson(gj)
      assertEquals(codable.fromJson(encoded), Right(gj))
      assertEquals(codable.parse(encoded.toString), Right(gj))
    }
  }
}
