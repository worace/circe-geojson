package works.worace.geojson

class FeatureCodecTest extends munit.FunSuite with TestHelpers {
  import TestData._
  import FeatureCodec.implicits._

  test("round-tripping base cases") {
    FeatureCases.all.foreach { c => codecCase[Feature](c) }
  }
}
