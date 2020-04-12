package works.worace.geojson

case class BBox(min: Coordinate, max: Coordinate) {
  def vec: Vector[Double] = {
    min.vec ++ max.vec
  }
}
