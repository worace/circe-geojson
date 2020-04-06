package works.worace.geojson

import io.circe._
import io.circe.parser._
import io.circe.syntax._

trait Codable[T <: GeoJson] {
  def codec: Codec[T]
  def parse(rawJson: String): Either[io.circe.Error, T] = {
    decode[T](rawJson)(codec.decoder)
  }

  def fromJson(json: Json): Either[io.circe.Error, T] = {
    json.as[T](codec.decoder)
  }

  def asJson(gj: T): Json = {
    gj.asJson(codec.encoder)
  }
}
