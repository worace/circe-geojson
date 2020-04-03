package works.worace.geojson.core

import org.scalatest.FeatureSpec
import io.circe.{Json, JsonObject, Decoder, DecodingFailure}

class PermutationsTest extends munit.FunSuite with TestHelpers {
  import TestData._

  test("Points") {
    for (p <- Permutations.points) {
      roundTripCase(p)
    }
  }

  test("LineStrings") {
    for (ls <- Permutations.lineStrings) {
      roundTripCase(ls)
    }
  }

  test("Polygons") {
    for (p <- Permutations.polygons) {
      roundTripCase(p)
    }
  }

  test("MultiPoints") {
    for (mp <- Permutations.multiPoints) {
      roundTripCase(mp)
    }
  }

  test("MultiLineStrings") {
    for (mls <- Permutations.multiLineStrings) {
      roundTripCase(mls)
    }
  }

  test("MultiPolygons") {
    for (mp <- Permutations.multiPolygons) {
      roundTripCase(mp)
    }
  }

  test("Features") {
    for (f <- Permutations.features) {
      roundTripCase(f)
    }
  }
}
