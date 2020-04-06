package works.worace.geojson

import io.circe._
import io.circe.syntax._

object CoordinateCodec {
  object implicits {
    implicit val coordinateEncoder = encoder
    implicit val coordinateDecoder = decoder
  }

  val encoder: Encoder[Coordinate] = Encoder.instance {
    // TODO - how does this handle large numbers that circe can't represent as JsonNumber
    coord => Json.arr(coord.array.flatMap(Json.fromDouble): _*)
  }

  val decoder: Decoder[Coordinate] = new Decoder[Coordinate] {
    final def apply(c: HCursor): Decoder.Result[Coordinate] = {
      c.as[Array[Double]]
        .filterOrElse(
          coords => coords.size > 1 && coords.size < 5,
          DecodingFailure("Invalid GeoJson Coordinates", c.history)
        )
        .map {
          case Array(x, y, z, m) => Coordinate(x, y, Some(z), Some(m))
          case Array(x, y, z)    => Coordinate(x, y, Some(z), None)
          case Array(x, y)       => Coordinate(x, y, None, None)
        }
    }
  }
}
