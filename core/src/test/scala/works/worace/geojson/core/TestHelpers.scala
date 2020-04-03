package works.worace.geojson.core

trait TestHelpers extends munit.FunSuite {
  import io.circe.syntax._
  import GeoJsonSerde._
  def roundTripCase(gj: GeoJson) {
    val encoded = gj.asJson
    GeoJson
      .fromJson(encoded)
      .map { decoded => assertEquals(decoded, gj) }
      .toOption
      .getOrElse(fail(s"Failed round-trip: ${gj}"))
  }
}
