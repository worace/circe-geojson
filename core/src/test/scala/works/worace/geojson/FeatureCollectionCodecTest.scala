package works.worace.geojson

class FeatureCollectionCodecTest extends munit.FunSuite with TestHelpers {
  import TestData._
  import FeatureCollectionCodec.implicits._

  test("round-tripping base cases") {
    FeatureCollectionCases.all.foreach { c => codecCase[FeatureCollection](c) }
  }
}
