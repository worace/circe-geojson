package works.worace.geojson

import io.circe._
import io.circe.syntax._
import io.circe.generic.extras.auto._
import io.circe.generic.extras.semiauto._
import TypeDiscriminator._
import CoordinateCodec.implicits._
import IdCodec.implicits._
import BBoxCodec.implicits._
import GeometryCodec.implicits._

private object FeatureCodec extends GjCodec[Feature] {
  object implicits {
    implicit val featureEncoder: Encoder[Feature] = encoder
    implicit val featureDecoder = decoder
  }
  private val base = deriveConfiguredDecoder[Feature]
  val decoder: Decoder[Feature] = Decoder.instance { cursor =>
    decodeWithForeignMembers(cursor, base, (f, fMembers) => f.copy(foreignMembers = fMembers))
  }
}
