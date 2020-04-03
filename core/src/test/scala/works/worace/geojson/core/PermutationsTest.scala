package works.worace.geojson.core

import org.scalatest.FeatureSpec
import io.circe.{Json, JsonObject, Decoder, DecodingFailure}

class PermutationsTest extends FeatureSpec with TestHelpers {
  import TestData._

  feature("Round Trip Permutations") {
    scenario("Points") {
      for (p <- Permutations.points) {
        roundTripCase(p)
      }
    }

    scenario("LineStrings") {
      for (ls <- Permutations.lineStrings) {
        roundTripCase(ls)
      }
    }

    scenario("Polygons") {
      for (p <- Permutations.polygons) {
        roundTripCase(p)
      }
    }

    scenario("MultiPoints") {
      for (mp <- Permutations.multiPoints) {
        roundTripCase(mp)
      }
    }

    scenario("MultiLineStrings") {
      for (mls <- Permutations.multiLineStrings) {
        roundTripCase(mls)
      }
    }

    scenario("MultiPolygons") {
      for (mp <- Permutations.multiPolygons) {
        roundTripCase(mp)
      }
    }
  }
}
