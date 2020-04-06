package works.worace.geojson

import io.circe._
import io.circe.generic.extras.auto._
import io.circe.generic.extras.semiauto._
import TypeDiscriminator._
import CoordinateCodec.implicits._
import BBoxCodec.implicits._
import IdCodec.implicits._

object GeoJsonCodec extends Codec[GeoJson] {
  object implicits {
    implicit val geoJsonEncoder = encoder
    implicit val geoJsonDecoder = decoder
  }

  def asJsonObject(gj: GeoJson): JsonObject = {
    gj match {
      case geom: Geometry        => GeometryCodec.asJsonObject(geom)
      case f: Feature            => FeatureCodec.asJsonObject(f)
      case fc: FeatureCollection => FeatureCollectionCodec.asJsonObject(fc)
    }
  }

  private val base: Decoder[GeoJson] = deriveConfiguredDecoder[GeoJson]
  val decoder: Decoder[GeoJson] = Decoder.instance { cursor =>
    decodeWithForeignMembers(
      cursor,
      base,
      (gj, fMembers) => {
        gj match {
          case geom: Geometry        => geom.withForeignMembers(fMembers)
          case f: Feature            => f.withForeignMembers(fMembers)
          case fc: FeatureCollection => fc.withForeignMembers(fMembers)
        }
      }
    )
  }
}
