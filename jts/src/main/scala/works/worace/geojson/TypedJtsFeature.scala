package works.worace.geojson.jts

import org.locationtech.jts.geom.Geometry

case class TypedJtsFeature[T](id: Option[String], properties: T, geometry: Geometry)
