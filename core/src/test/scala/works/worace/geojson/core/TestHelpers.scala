package works.worace.geojson.core

import org.scalatest.FeatureSpec

trait TestHelpers extends FeatureSpec {
  import io.circe.syntax._
  import GeoJsonSerde._
  def roundTripCase(gj: GeoJson) {
    val encoded = gj.asJson
    GeoJson
      .fromJson(encoded)
      .map { decoded => assert(decoded == gj) }
      .toOption
      .getOrElse(fail(s"Failed round-trip: ${gj}"))
  }
}
