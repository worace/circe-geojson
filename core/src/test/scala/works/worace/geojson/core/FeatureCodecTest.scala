package works.worace.geojson.core

class FeatureCodecTest extends munit.FunSuite with TestHelpers{
  import TestData._
  import FeatureCodec.Implicits._

  test("round-tripping base cases") {
    FeatureCases.all.foreach { c => codecCase[Feature](c) }
  }
}
