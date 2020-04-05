package works.worace.geojson.core

import io.circe._
import io.circe.syntax._
import io.circe.generic.extras.auto._
import io.circe.generic.extras.semiauto._
import TypeDiscriminator._
import FeatureCodec._
import BBoxCodec.implicits._
import CoordinateCodec.implicits._
import IdCodec.implicits._

object FeatureCollectionCodec extends Codec[FeatureCollection] {
  object Implicits {
    implicit val featureCollectionEncoder = encoder
    implicit val featureCollectionDecoder = decoder
  }
  private val base = deriveConfiguredDecoder[FeatureCollection]
  val decoder: Decoder[FeatureCollection] = Decoder.instance { cursor =>
    decodeWithForeignMembers(cursor, base, (f, fMembers) => f.copy(foreignMembers=fMembers))
  }

  private val fcBase = JsonObject("type" -> Json.fromString("FeatureCollection"))
  def asJsonObject(fc: FeatureCollection): JsonObject = {
    import io.circe.syntax._
    val withFeatures = fcBase.add("features", fc.features.asJson)
    fc.bbox match {
      case None       => withFeatures
      case Some(bbox) => withFeatures.add("bbox", bbox.asJson)
    }
  }
}
