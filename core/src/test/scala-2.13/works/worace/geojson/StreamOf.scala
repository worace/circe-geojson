package works.worace.geojson

object StreamOf {
  def streamOf[T](f: => T): LazyList[T] = f #:: streamOf(f)
}
