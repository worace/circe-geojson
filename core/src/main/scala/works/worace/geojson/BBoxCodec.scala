package works.worace.geojson

import io.circe._
import io.circe.syntax._
import CoordinateCodec.implicits._

object BBoxCodec {
  object implicits {
    implicit val bboxEncoder = encoder
    implicit val bboxDecoder = decoder
  }
  val encoder: Encoder[BBox] = Encoder.instance { bbox => bbox.flat.asJson }

  val decoder: Decoder[BBox] = new Decoder[BBox] {
    final def apply(c: HCursor): Decoder.Result[BBox] = {
      c.as[Array[Double]].flatMap {
        case Array(x1, y1, x2, y2) => Right(BBox(Coordinate(x1, y1), Coordinate(x2, y2)))
        case Array(x1, y1, z1, x2, y2, z2) =>
          Right(BBox(Coordinate(x1, y1, z1), Coordinate(x2, y2, z2)))
        case _ => Left(DecodingFailure("Invalid GeoJson BBox", c.history))
      }
    }
  }
}
