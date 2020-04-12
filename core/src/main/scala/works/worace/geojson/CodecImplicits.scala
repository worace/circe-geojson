package works.worace.geojson

import io.circe.{Encoder, Decoder}

/**
  * Wrapper object providing implicit values for GeoJson encoder/decoder instances.
  *
  * Use this object if you want a wildcard-import of implicit Circe encoder/decoder values
  * in order to use GeoJson types directly with Circe's syntax.
  *
  * In many cases, these imports won't be needed, since you can go through the relevant [[Codable]]
  * methods: [[Codable.parse]], [[Codable.fromJson]], or [[Codable.asJson]].
  *
  * But if you prefer the Circe syntax style, this import should give you the implicits you need:
  *
  *  {{{
  *  scala> import io.circe.syntax._
  *  scala> import io.circe.parser.decode
  *  scala> import works.worace.geojson.CodecImplicits._
  *  scala> decode[Point]("""{"type":"Point","coordinates":[1.0,-1.0]}""")
  *  scala> decode[GeoJson]("""{"type":"Point","coordinates":[1.0,-1.0]}""")
  *  }}}
  */
object CodecImplicits {
  implicit val geojsonEncoder: Encoder[GeoJson] = GeoJsonCodec.encoder
  implicit val geojsonDecoder: Decoder[GeoJson] = GeoJsonCodec.decoder

  implicit val geometryEncoder: Encoder[Geometry] = GeometryCodec.encoder
  implicit val geometryDecoder: Decoder[Geometry] = GeometryCodec.decoder

  implicit val featureEncoder: Encoder[Feature] = FeatureCodec.encoder
  implicit val featureDecoder: Decoder[Feature] = FeatureCodec.decoder

  implicit val featureCollectionEncoder: Encoder[FeatureCollection] = FeatureCollectionCodec.encoder
  implicit val featureCollectionDecoder: Decoder[FeatureCollection] = FeatureCollectionCodec.decoder

  implicit val pointEncoder: Encoder[Point] = PointCodec.encoder
  implicit val pointDecoder: Decoder[Point] = PointCodec.decoder

  implicit val lineStringEncoder: Encoder[LineString] = LineStringCodec.encoder
  implicit val lineStringDecoder: Decoder[LineString] = LineStringCodec.decoder

  implicit val polygonEncoder: Encoder[Polygon] = PolygonCodec.encoder
  implicit val polygonDecoder: Decoder[Polygon] = PolygonCodec.decoder

  implicit val MultiPointEncoder: Encoder[MultiPoint] = MultiPointCodec.encoder
  implicit val MultiPointDecoder: Decoder[MultiPoint] = MultiPointCodec.decoder

  implicit val multiLineStringEncoder: Encoder[MultiLineString] = MultiLineStringCodec.encoder
  implicit val multiLineStringDecoder: Decoder[MultiLineString] = MultiLineStringCodec.decoder

  implicit val multiPolygonEncoder: Encoder[MultiPolygon] = MultiPolygonCodec.encoder
  implicit val multiPolygonDecoder: Decoder[MultiPolygon] = MultiPolygonCodec.decoder

  implicit val geometryCollectionEncoder: Encoder[GeometryCollection] = GeometryCollectionCodec.encoder
  implicit val geometryCollectionDecoder: Decoder[GeometryCollection] = GeometryCollectionCodec.decoder
}
