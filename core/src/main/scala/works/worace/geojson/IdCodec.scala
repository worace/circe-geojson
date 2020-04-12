package works.worace.geojson

import io.circe._

private object IdCodec {
  type ID = Either[Long, String]

  object implicits {
    // https://github.com/circe/circe/issues/672
    implicit def encodeEither[A, B](
      implicit
      encoderA: Encoder[A],
      encoderB: Encoder[B]
    ): Encoder[Either[A, B]] = {
      import io.circe.syntax._
      o: Either[A, B] => o.fold(_.asJson, _.asJson)
    }

    implicit def decodeEither[A, B](
      implicit
      decoderA: Decoder[A],
      decoderB: Decoder[B]
    ): Decoder[Either[A, B]] = decoderA.either(decoderB)
  }
}
