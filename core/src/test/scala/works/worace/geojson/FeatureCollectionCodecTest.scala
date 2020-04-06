package works.worace.geojson.core

class FeatureCollectionCodecTest extends munit.FunSuite with TestHelpers {
  import TestData._
  import FeatureCollectionCodec.Implicits._

  test("round-tripping base cases") {
    FeatureCollectionCases.all.foreach { c => codecCase[FeatureCollection](c) }
  }
}
