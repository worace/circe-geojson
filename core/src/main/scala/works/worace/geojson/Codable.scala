package works.worace.geojson

import io.circe._
import io.circe.parser._
import io.circe.Decoder.Result
import io.circe.syntax._

/**
  * Interface for encoding and decoding [[GeoJson]] values.
  *
  * Codable objects provide convenience methods for parsing a given GeoJson type
  * (from a String), decoding (from an already-parsed Circe Json value),
  * and encoding (to a Circe Json value).
  *
  * Additionally, Codable objects expose Circe Encoder and Decoder instances for
  * their appropriate type, which can be used with Circe's standard syntax directly if preferred.
  *
  * Codable types include the 2 GeoJson ADTs:
  *
  *  - [[GeoJson]]
  *  - [[Geometry]]
  *
  * As well as their concrete subtypes:
  *
  *  - [[FeatureCollection]]
  *  - [[Feature]]
  *  - [[Point]]
  *  - [[LineString]]
  *  - [[Polygon]]
  *  - [[MultiPoint]]
  *  - [[MultiLineString]]
  *  - [[MultiPolygon]]
  *  - [[GeometryCollection]]
  *
  */
trait Codable[T <: GeoJson] {
  val encoder: Encoder[T]
  val decoder: Decoder[T]

  def parse(rawJson: String): Either[io.circe.Error, T] = {
    decode[T](rawJson)(decoder)
  }

  def fromJson(json: Json): Either[io.circe.Error, T] = {
    json.as[T](decoder)
  }

  def asJson(gj: T): Json = {
    gj.asJson(encoder)
  }
}
