package works.worace.geojson.jts

import org.locationtech.jts.geom.Geometry
import io.circe.{Json, JsonObject, Decoder}

case class TypedJtsFeature[T](id: Option[String], properties: T, geometry: Geometry)

case class JtsFeature(id: Option[String], properties: JsonObject, geometry: Geometry) {
  def typed[T](implicit decoder: Decoder[T]): Decoder.Result[TypedJtsFeature[T]] = {
    Json.fromJsonObject(properties).as[T].map(props => TypedJtsFeature(id, props, geometry))
  }
}
