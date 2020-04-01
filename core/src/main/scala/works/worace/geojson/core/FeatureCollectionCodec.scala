package works.worace.geojson.core

import io.circe._
import io.circe.syntax._
import io.circe.generic.extras.auto._
import io.circe.generic.extras.semiauto._
import TypeDiscriminator._
import FeatureCodec._
import BBoxSerde._
import CoordinateSerde._
import IdSerde._

object FeatureCollectionCodec {
  implicit val decoder: Decoder[FeatureCollection] = deriveConfiguredDecoder[FeatureCollection]
  implicit val encoder: Encoder[FeatureCollection] = Encoder.instance { fc =>
    import io.circe.syntax._
    asJsonObj(fc).asJson
  }

  private val fcBase = JsonObject("type" -> Json.fromString("FeatureCollection"))
  private def asJsonObj(fc: FeatureCollection): JsonObject = {
    import io.circe.syntax._
    val withFeatures = fcBase.add("features", fc.features.asJson)
    fc.bbox match {
      case None       => withFeatures
      case Some(bbox) => withFeatures.add("bbox", bbox.asJson(BBoxSerde.bboxEncoder))
    }
  }
}
