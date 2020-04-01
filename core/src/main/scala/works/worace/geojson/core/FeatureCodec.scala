package works.worace.geojson.core

import io.circe._
import io.circe.syntax._
import io.circe.generic.extras.auto._
import io.circe.generic.extras.semiauto._
import TypeDiscriminator._
import CoordinateSerde._
import IdSerde._
import GeometryCodec._
import BBoxSerde._

object FeatureCodec {
  implicit val decoder: Decoder[Feature] = deriveConfiguredDecoder[Feature]
  implicit val encoder: Encoder[Feature] = Encoder.instance { f =>
    import io.circe.syntax._
    asJsonObj(f).asJson
  }

  private val featureBase = JsonObject("type" -> Json.fromString("Feature"))
  private def asJsonObj(f: Feature): JsonObject = {
    List(
      f.id.map(id => ("id", id.asJson(IdSerde.encodeEither))),
      f.properties.map(p => ("properties", p.asJson)),
      f.bbox.map(bb => ("bbox", bb.asJson(BBoxSerde.bboxEncoder))),
      f.geometry.map((g: Geometry) => ("geometry", g.asJson))
    ).flatten
      .foldLeft(featureBase)(
        (feature: JsonObject, pair: (String, Json)) => feature.add(pair._1, pair._2)
      )
  }
}
