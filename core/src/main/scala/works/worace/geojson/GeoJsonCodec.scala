package works.worace.geojson

import io.circe.Decoder
import io.circe.generic.extras.auto._
import io.circe.generic.extras.semiauto._
import TypeDiscriminator._
import CoordinateCodec.implicits._
import BBoxCodec.implicits._
import IdCodec.implicits._
import GeometryCodec.implicits._
import FeatureCodec.implicits._
import FeatureCollectionCodec.implicits._

private object GeoJsonCodec extends GjCodec[GeoJson] {
  object implicits {
    implicit val geoJsonEncoder = encoder
    implicit val geoJsonDecoder = decoder
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
