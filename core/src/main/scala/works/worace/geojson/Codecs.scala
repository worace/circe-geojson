package works.worace.geojson.core

import io.circe.{Encoder, Decoder}

object Codecs {
  implicit val geomEncoder: Encoder[Geometry] = GeometryCodec.encoder
  implicit val geomDecoder: Decoder[Geometry] = GeometryCodec.decoder
  implicit val featureEncoder: Encoder[Feature] = FeatureCodec.encoder
  implicit val featureDecoder: Decoder[Feature] = FeatureCodec.decoder
  implicit val fcEncoder: Encoder[FeatureCollection] = FeatureCollectionCodec.encoder
  implicit val fcDecoder: Decoder[FeatureCollection] = FeatureCollectionCodec.decoder
  implicit val bboxEncoder: Encoder[BBox] = BBoxCodec.encoder
  implicit val bboxDecoder: Decoder[BBox] = BBoxCodec.decoder
}
