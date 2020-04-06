package works.worace.geojson

import io.circe.{Json, JsonObject, Decoder, DecodingFailure}
import io.circe.parser.decode
import scala.reflect.ClassTag

class GeometryCodecTest extends munit.FunSuite with TestHelpers {
  import TestData._
  import GeometryCodec.implicits._

  test("round-tripping base cases") {
    BaseGeomCases.all.foreach { c => codecCase[Geometry](c) }
  }

  test("round-tripping base cases as GeoJson") {
    import GeoJsonCodec.implicits._
    BaseGeomCases.all.foreach { c => codecCase[GeoJson](c) }
  }

  test("round-tripping permutations as geometries") {
    import GeometryCodec.implicits._
    Permutations.allGeomOpts.foreach { g => roundTripCodecCase[Geometry](g) }
  }

  test("round-tripping permutations as GeoJson") {
    import GeoJsonCodec.implicits._
    Permutations.allGeomOpts.foreach { g => roundTripCodecCase[GeoJson](g) }
  }

  test("per-geom codec permutation round trips") {
    codecPermutationTests(Permutations.points, PointCodec)
    codecPermutationTests(Permutations.lineStrings, LineStringCodec)
    codecPermutationTests(Permutations.polygons, PolygonCodec)
    codecPermutationTests(Permutations.multiPoints, MultiPointCodec)
    codecPermutationTests(Permutations.multiLineStrings, MultiLineStringCodec)
    codecPermutationTests(Permutations.multiPolygons, MultiPolygonCodec)
    codecPermutationTests(Permutations.geometryCollections, GeometryCollectionCodec)
  }

  def codecPermutationTests[G <: GeoJson : ClassTag](permutations: Vector[G], codec: Codec[G]) = {
    implicit val decoder = codec.decoder
    implicit val encoder = codec.encoder
    permutations.foreach { g => roundTripCodecCase[G](g) }
  }
}
