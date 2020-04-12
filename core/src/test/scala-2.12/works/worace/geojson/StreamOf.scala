package works.worace.geojson

object StreamOf {
  def streamOf[T](f: => T): Stream[T] = f #:: streamOf(f)
}
