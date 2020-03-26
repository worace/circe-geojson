package works.worace.geojson.jts

import org.locationtech.jts.geom.Geometry
import io.circe.JsonObject

case class JtsFeature(id: Option[String], properties: JsonObject, geometry: Geometry)
