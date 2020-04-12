package works.worace.geojson

import io.circe._
import io.circe.syntax._
import io.circe.generic.extras.auto._
import io.circe.generic.extras.semiauto._
import io.circe.generic.extras._

private object GeometryCollectionCodec extends GjCodec[GeometryCollection] {
  import TypeDiscriminator._
  import CoordinateCodec.implicits._
  import BBoxCodec.implicits._
  import PointCodec.implicits._
  import LineStringCodec.implicits._
  import PolygonCodec.implicits._
  import MultiPointCodec.implicits._
  import MultiLineStringCodec.implicits._
  import MultiPolygonCodec.implicits._

  object implicits {
    implicit val geometryCollectionEncoder = encoder
    implicit val geometryCollectionDecoder = decoder
  }

  private val base: Decoder[GeometryCollection] = deriveConfiguredDecoder[GeometryCollection]
  val decoder: Decoder[GeometryCollection] = Decoder.instance { cursor =>
    decodeWithForeignMembers(cursor, base, (geom, fMembers) => {
      geom.withForeignMembers(fMembers)
    })
  }
}
