package works.worace.geojson

case class BBox(min: Coordinate, max: Coordinate) {
  def flat: Array[Double] = {
    min.array ++ max.array
  }
}
