package works.worace.geojson.core

import io.circe._
import io.circe.syntax._
import io.circe.generic.extras.auto._
import io.circe.generic.extras.semiauto._
import TypeDiscriminator._
import CoordinateCodec.implicits._
import IdSerde._
import GeometryCodec._
import BBoxCodec.implicits._

object FeatureCodec {
  object Implicits {
    implicit val featureEncoder = encoder
    implicit val featureDecoder = decoder
  }
  val decoder: Decoder[Feature] = deriveConfiguredDecoder[Feature]
  val encoder: Encoder[Feature] = Encoder.instance { f =>
    import io.circe.syntax._
    asJsonObj(f).asJson
  }

  private val featureBase = JsonObject("type" -> Json.fromString("Feature"))
  private def asJsonObj(f: Feature): JsonObject = {
    val start = f.foreignMembers.getOrElse(JsonObject()).deepMerge(featureBase)
    List(
      f.id.map(id => ("id", id.asJson(IdSerde.encodeEither))),
      f.properties.map(p => ("properties", p.asJson)),
      f.bbox.map(bb => ("bbox", bb.asJson)),
      f.geometry.map((g: Geometry) => ("geometry", g.asJson))
    ).flatten
      .foldLeft(start)((feature: JsonObject, pair: (String, Json)) =>
        feature.add(pair._1, pair._2)
      )
  }
}
