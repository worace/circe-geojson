package works.worace.geojson

import io.circe.generic.extras.Configuration

private object TypeDiscriminator {
  implicit val discriminator = Configuration.default.withDiscriminator("type")
}
