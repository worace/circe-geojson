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
  }
}
