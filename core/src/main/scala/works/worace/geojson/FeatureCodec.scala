package works.worace.geojson.core

import io.circe._
import io.circe.syntax._
import io.circe.generic.extras.auto._
import io.circe.generic.extras.semiauto._
import TypeDiscriminator._
import CoordinateCodec.implicits._
import IdCodec.implicits._
import GeometryCodec._
import BBoxCodec.implicits._

object FeatureCodec extends Codec[Feature] {
  object Implicits {
    implicit val featureEncoder = encoder
    implicit val featureDecoder = decoder
  }
  private val base = deriveConfiguredDecoder[Feature]
  val decoder: Decoder[Feature] = Decoder.instance { cursor =>
    decodeWithForeignMembers(cursor, base, (f, fMembers) => f.copy(foreignMembers = fMembers))
  }

  private val featureBase = JsonObject("type" -> Json.fromString("Feature"))
  def asJsonObject(f: Feature): JsonObject = {
    val start = f.foreignMembers.getOrElse(JsonObject()).deepMerge(featureBase)
    List(
      f.id.map(id => ("id", id.asJson)),
      f.properties.map(p => ("properties", p.asJson)),
      f.bbox.map(bb => ("bbox", bb.asJson)),
      f.geometry.map((g: Geometry) => ("geometry", g.asJson))
    ).flatten
      .foldLeft(start)((feature: JsonObject, pair: (String, Json)) => feature.add(pair._1, pair._2))
  }
}
