package works.worace.geojson

import io.circe._
import io.circe.syntax._
import io.circe.generic.extras.auto._
import io.circe.generic.extras.semiauto._
import TypeDiscriminator._
import FeatureCodec.implicits._
import BBoxCodec.implicits._
import CoordinateCodec.implicits._
import GeometryCodec.implicits._
import IdCodec.implicits._

private object FeatureCollectionCodec extends Codec[FeatureCollection] {
  object implicits {
    implicit val featureCollectionEncoder = encoder
    implicit val featureCollectionDecoder = decoder
  }
  private val base = deriveConfiguredDecoder[FeatureCollection]
  val decoder: Decoder[FeatureCollection] = Decoder.instance { cursor =>
    decodeWithForeignMembers(cursor, base, (f, fMembers) => f.copy(foreignMembers = fMembers))
  }
}
