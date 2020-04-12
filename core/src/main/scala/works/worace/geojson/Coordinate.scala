package works.worace.geojson

case class Coordinate(x: Double, y: Double, z: Option[Double], m: Option[Double]) {
  def vec: Vector[Double] = {
    this match {
      case Coordinate(x, y, None, None)       => Vector(x, y)
      case Coordinate(x, y, Some(z), None)    => Vector(x, y, z)
      case Coordinate(x, y, Some(z), Some(m)) => Vector(x, y, z, m)
      // TODO: What is right here? Should ideally prevent constructing Coord with x,y,m but no z
      case Coordinate(x, y, None, Some(_)) => Vector(x, y)
    }
  }
}

object Coordinate {
  def apply(x: Double, y: Double): Coordinate = {
    Coordinate(x, y, None, None)
  }

  def apply(x: Double, y: Double, z: Double): Coordinate = {
    Coordinate(x, y, Some(z), None)
  }

  def apply(x: Double, y: Double, z: Double, m: Double): Coordinate = {
    Coordinate(x, y, Some(z), Some(m))
  }
}
