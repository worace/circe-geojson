package works.worace.geojson.core.codecs

import io.circe.generic.extras.Configuration

object TypeDiscriminator {
  implicit val discriminator = Configuration.default.withDiscriminator("type")
}
