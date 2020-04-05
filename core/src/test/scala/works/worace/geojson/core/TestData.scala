package works.worace.geojson.core

import io.circe.{Json, JsonObject, Decoder, DecodingFailure}
import io.circe.JsonNumber

object TestData {
  val coordXY = "[102.0, 0.5]"
  val coordXYZ = "[102.0, 0.5, 1.0]"
  val coordXYZM = "[102.0, 0.5, 1.0, 2.0]"
  val coordTooFew = "[102.0]"
  val coordTooMany = "[102.0, 0.5, 1.0, 2.0, 3.0]"

  def rand: Double = scala.util.Random.nextDouble * 1000
  def randInt(i: Int = 10): Int = scala.util.Random.nextInt(i) + 1

  def genCoordXY: Coordinate = Coordinate(rand, rand)
  def genCoordXYZ: Coordinate = Coordinate(rand, rand, rand)
  def genCoordXYZM: Coordinate = Coordinate(rand, rand, rand, rand)
  def streamOf[T](f: => T): Stream[T] = f #:: streamOf(f)
  def nOf[T](f: => T, n: Int): Vector[T] = streamOf(f).take(n).toVector
  def coordSeqXY: Stream[Coordinate] = streamOf(genCoordXY) //genCoordXY #:: coordSeqXY
  def coordSeqXYZ: Stream[Coordinate] = streamOf(genCoordXYZ)
  def coordSeqXYZM: Stream[Coordinate] = streamOf(genCoordXYZM)
  def pointXY: Point = Point(genCoordXY)
  def pointXYZ: Point = Point(genCoordXYZ)
  def pointXYZM: Point = Point(genCoordXYZM)
  def linestringXY: LineString = LineString(coordSeqXY.take(randInt() + 1))
  def linestringXYZ: LineString = LineString(coordSeqXY.take(randInt() + 1))
  def linestringXYZM: LineString = LineString(coordSeqXY.take(randInt() + 1))

  def times[A](n: Int, f: => A): Vector[A] = (1 to n).toVector.map(_ => f)

  def multiLineStringXY: MultiLineString =
    MultiLineString((1 to randInt() + 1).map(_ => coordSeqXY.take(randInt() + 1)))

  def polygonXY(numRings: Int = 2): Polygon = {
    val rings = (1 to numRings).map { _ => closedRingXY }.toVector
    Polygon(rings)
  }

  def closedRing(coords: Stream[Coordinate]): Vector[Coordinate] = {
    val seq = coords.take(randInt() + 3).toVector
    seq :+ seq.head
  }

  def closedRingXY: Vector[Coordinate] = closedRing(coordSeqXY)
  def closedRingXYZ: Vector[Coordinate] = closedRing(coordSeqXYZ)
  def closedRingXYZM: Vector[Coordinate] = closedRing(coordSeqXYZM)

  def multiPolygonXY: MultiPolygon = {
    val rings = times(randInt(), {
      times(randInt(), closedRingXY)
    })
    MultiPolygon(rings)
  }

  def bboxXY: BBox = BBox(genCoordXY, genCoordXY)
  def bboxXYZ: BBox = BBox(genCoordXYZ, genCoordXYZ)

  def bboxOpts: Vector[Option[BBox]] = {
    Vector(None, Some(bboxXY), Some(bboxXYZ))
  }

  def coordSeqOpts: Vector[Vector[Coordinate]] = {
    Vector(coordSeqXY, coordSeqXYZ, coordSeqXYZM).map(s => s.take(randInt() + 1).toVector)
  }

  def coordSeqSeqOpts: Vector[Vector[Vector[Coordinate]]] = {
    val numPoints = randInt() + 1
    val numLines = randInt()
    Vector(
      nOf(coordSeqXY.take(numPoints).toVector, numLines),
      nOf(coordSeqXYZ.take(numPoints).toVector, numLines),
      nOf(coordSeqXYZM.take(numPoints).toVector, numLines)
    )
  }

  def closedRingOpts: Vector[Vector[Coordinate]] = {
    Vector(closedRingXY, closedRingXYZ, closedRingXYZM)
  }

  def closedRingSeqOpts: Vector[Vector[Vector[Vector[Coordinate]]]] = {
    Vector(
      //     outer                       , ... inner
      Vector(nOf(closedRingXY, randInt()), nOf(closedRingXY, randInt())),
      Vector(nOf(closedRingXYZ, randInt()), nOf(closedRingXYZ, randInt())),
      Vector(nOf(closedRingXYZM, randInt()), nOf(closedRingXYZM, randInt()))
    )
  }

  def jsonObject: JsonObject = {
    JsonObject("a" -> Json.fromString("b"))
  }

  def fmemberOpts: List[Option[JsonObject]] = {
    List(None, Some(jsonObject))
  }

  object Permutations {
    def points: Vector[Point] = {
      for {
        bbox <- bboxOpts
        coords <- coordSeqOpts
        fmember <- fmemberOpts
      } yield Point(coords.head, bbox, fmember)
    }

    def lineStrings: Vector[LineString] = {
      for {
        bbox <- bboxOpts
        coords <- coordSeqOpts
        fmember <- fmemberOpts
      } yield LineString(coords, bbox, fmember)
    }

    def polygons: Vector[Polygon] = {
      for {
        bbox <- bboxOpts
        outer <- closedRingOpts
        fmember <- fmemberOpts
      } yield Polygon(Vector(outer), bbox, fmember)
    }

    def multiPoints: Vector[MultiPoint] = {
      val numPoints = randInt(5)
      for {
        coords <- coordSeqOpts
        bbox <- bboxOpts
        fmember <- fmemberOpts
      } yield MultiPoint(coords, bbox, fmember)
    }

    def multiLineStrings: Vector[MultiLineString] = {
      for {
        coords <- coordSeqSeqOpts
        bbox <- bboxOpts
        fmember <- fmemberOpts
      } yield MultiLineString(coords, bbox, fmember)
    }

    def multiPolygons: Vector[MultiPolygon] = {
      for {
        coords <- closedRingSeqOpts
        bbox <- bboxOpts
        fmember <- fmemberOpts
      } yield MultiPolygon(coords, bbox, fmember)
    }

    def allGeomOpts: Vector[Geometry] = {
      scala.util.Random.shuffle(points ++ lineStrings ++ polygons ++ multiPoints ++ multiLineStrings ++ multiPolygons)
    }

    def idOpts: Vector[Option[Either[JsonNumber, String]]] = {
      Vector(
        None,
        Some(Left(JsonNumber.fromString(randInt().toString).get)),
        Some(Right(randInt().toString))
      )
    }

    def propOpts: Vector[Option[JsonObject]] = {
      Vector(None, Some(jsonObject))
    }

    def features: Vector[Feature] = {
      val geoms = allGeomOpts.take(randInt(5)).map(Some(_)) :+ None
      for {
        id <- idOpts
        properties <- propOpts
        geom <- geoms
        bbox <- bboxOpts
        fmembers <- fmemberOpts
      } yield Feature(id, properties, geom, bbox, fmembers)
    }
  }

  case class Case(
    encoded: String,
    decoded: GeoJson
  )

  object BaseGeomCases {
    val point = Case(
      """{"type": "Point", "coordinates": [102.0, 0.5]}""",
      Point(Coordinate(102.0, 0.5))
    )
    val lineString = Case(
      """{"type": "LineString", "coordinates": [[1.0, 2.0], [2.0, 3.0]]}""",
      LineString(Vector(Coordinate(1.0, 2.0), Coordinate(2.0, 3.0)))
    )
    val polygon = Case(
      """
        {"type": "Polygon", "coordinates": [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]]]}
      """,
      Polygon(
        Vector(
          Vector(
            Coordinate(100.0, 0.0),
            Coordinate(101.0, 0.0),
            Coordinate(101.0, 1.0),
            Coordinate(100.0, 1.0),
            Coordinate(100.0, 0.0)
          )
        )
      )
    )

    val multiPoint = Case(
      """
      {"type": "MultiPoint", "coordinates": [ [100.0, 0.0], [101.0, 1.0]]}
    """,
      MultiPoint(Vector(Coordinate(100.0, 0.0), Coordinate(101.0, 1.0)))
    )
    val multiLineString = Case(
      """
      {"type": "MultiLineString",
       "coordinates": [
         [ [100.0, 0.0], [101.0, 1.0] ],
         [ [102.0, 2.0], [103.0, 3.0] ]
       ]
      }
    """,
      MultiLineString(
        Vector(
          Vector(Coordinate(100.0, 0.0), Coordinate(101.0, 1.0)),
          Vector(Coordinate(102.0, 2.0), Coordinate(103.0, 3.0))
        )
      )
    )
    val multiPolygon = Case(
      """
      {"type": "MultiPolygon",
       "coordinates": [
         [[[102.0, 2.0], [103.0, 2.0], [103.0, 3.0], [102.0, 3.0], [102.0, 2.0]]],
         [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]],
          [[100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]]
       ]
      }
    """,
      MultiPolygon(
        Vector(
          Vector(
            Vector(
              Coordinate(102.0, 2.0),
              Coordinate(103.0, 2.0),
              Coordinate(103.0, 3.0),
              Coordinate(102.0, 3.0),
              Coordinate(102.0, 2.0)
            )
          ),
          Vector(
            Vector(
              Coordinate(100.0, 0.0),
              Coordinate(101.0, 0.0),
              Coordinate(101.0, 1.0),
              Coordinate(100.0, 1.0),
              Coordinate(100.0, 0.0)
            ),
            Vector(
              Coordinate(100.2, 0.2),
              Coordinate(100.8, 0.2),
              Coordinate(100.8, 0.8),
              Coordinate(100.2, 0.8),
              Coordinate(100.2, 0.2)
            )
          )
        )
      )
    )
    val geometryCollection = Case(
      """
      { "type": "GeometryCollection",
        "geometries": [
          { "type": "Point", "coordinates": [101.0, 1.0]},
          { "type": "LineString", "coordinates": [ [101.0, 0.0], [102.0, 1.0] ]}
        ]
      }
      """,
      GeometryCollection(
        Vector(
          Point(Coordinate(101.0, 1.0)),
          LineString(Vector(Coordinate(101.0, 0.0), Coordinate(102.0, 1.0)))
        )
      )
    )

    val all: List[Case] = List(
      point,
      lineString,
      polygon,
      multiPoint,
      multiLineString,
      multiPolygon,
      geometryCollection
    )
  }

  object FeatureCases {
    val noProps = Case(
      """
      { "type": "Feature", "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}}
      """,
      Feature(
        Point(Coordinate(101.0, 1.0))
      )
    )

    val xyz = Case(
      """
      { "type": "Feature", "geometry": {"type": "Point", "coordinates": [101.0, 1.0, 2.0]}}
      """,
      Feature(
        Point(Coordinate(101.0, 1.0, 2.0))
      )
    )

    val xyzm = Case(
      """
      { "type": "Feature", "geometry": {"type": "Point", "coordinates": [101.0, 1.0, 2.0, 3.0]}}
      """,
      Feature(
        Point(Coordinate(101.0, 1.0, 2.0, 3.0))
      )
    )

    val nullProps = Case(
      """
      { "type": "Feature", "properties": null, "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}}
      """,
      Feature(
        Point(Coordinate(101.0, 1.0))
      )
    )

    val empty = Case(
      """
      { "type": "Feature"}
      """,
      Feature(None, None, None, None, None)
    )

    val nullGeom = Case(
      """
      { "type": "Feature", "geometry": null}
      """,
      Feature(None, None, None, None, None)
    )

    val props = Case(
      """
      { "type": "Feature", "properties": {"a": "b", "c": 1}, "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}}
      """,
      Feature(
        JsonObject("a" -> Json.fromString("b"), "c" -> Json.fromInt(1)),
        Point(Coordinate(101.0, 1.0))
      )
    )

    val stringId = Case(
      """ {"type": "Feature",
        "id": "123",
        "properties": {"a": "b"},
        "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}
      }
      """,
      Feature(
        "123",
        JsonObject("a" -> Json.fromString("b")),
        Point(Coordinate(101.0, 1.0))
      )
    )

    val intId = Case(
      """ {"type": "Feature",
        "id": 123,
        "properties": {"a": "b"},
        "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}
      }
      """,
      Feature(
        123,
        JsonObject("a" -> Json.fromString("b")),
        Point(Coordinate(101.0, 1.0))
      )
    )

    val bbox = Case(
      """{"type": "Feature",
        "bbox": [101.0, 1.0, 101.0, 1.0],
        "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}
      }
      """,
      Feature(
        None,
        None,
        Some(Point(Coordinate(101.0, 1.0))),
        Some(BBox(Coordinate(101.0, 1.0), Coordinate(101.0, 1.0))),
        None
      )
    )
  }

  object FeatureCollectionCases {
    val noForeignMembers = Case(
      """
      {"type": "FeatureCollection",
       "features": [
         {"type": "Feature",
          "id": "pizza",
          "properties": {"a": "b"},
          "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}
         }
        ]
      }
      """,
      FeatureCollection(
        Vector(
          Feature(
            "pizza",
            JsonObject("a" -> Json.fromString("b")),
            Point(Coordinate(101.0, 1.0))
          )
        )
      )
    )

    val foreignMembers = Case(
      """
      {"type": "FeatureCollection",
       "some": "prop",
       "features": [
         {"type": "Feature",
          "id": "pizza",
          "properties": {"a": "b"},
          "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}
         }
        ]
      }
      """,
      FeatureCollection(
        Vector(
          Feature(
            "pizza",
            JsonObject("a" -> Json.fromString("b")),
            Point(Coordinate(101.0, 1.0))
          )
        ),
        None,
        Some(JsonObject("some" -> Json.fromString("prop")))
      )
    )

    val bbox = Case(
      """
      {"type": "FeatureCollection",
       "bbox": [101.0, 1.0, 101.0, 1.0],
       "features": [
         {"type": "Feature",
          "id": "pizza",
          "properties": {"a": "b"},
          "geometry": {"type": "Point", "coordinates": [101.0, 1.0]}
         }
        ]
      }
      """,
      FeatureCollection(
        Vector(
          Feature(
            "pizza",
            JsonObject("a" -> Json.fromString("b")),
            Point(Coordinate(101.0, 1.0))
          )
        ),
        Some(BBox(Coordinate(101.0, 1.0), Coordinate(101.0, 1.0))),
        None
      )
    )
  }
}
