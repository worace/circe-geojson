package works.worace.geojson.core

import io.circe.{Json, JsonObject, Decoder, DecodingFailure}

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
  def coordSeqXY: Stream[Coordinate] = genCoordXY #:: coordSeqXY
  def coordSeqXYZ: Stream[Coordinate] = genCoordXYZ #:: coordSeqXYZ
  def coordSeqXYZM: Stream[Coordinate] = genCoordXYZM #:: coordSeqXYZM
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
    val rings = (1 to numRings).map { _ =>
      closedRingXY
    }.toVector
    Polygon(rings)
  }

  def closedRingXY: Vector[Coordinate] = {
    val seq = coordSeqXY.take(randInt() + 3).toVector
    seq :+ seq.head
  }

  def multiPolygonXY: MultiPolygon = {
    val rings = times(randInt(), {
      times(randInt(), closedRingXY)
    })
    MultiPolygon(rings)
  }

  def bboxXY: BBox = BBox(genCoordXY, genCoordXY)
  def bboxXYZ: BBox = BBox(genCoordXYZ, genCoordXYZ)

  def bboxOpts: List[Option[BBox]] = {
    List(None, Some(bboxXY), Some(bboxXYZ))
  }

  def coordSeqOpts: List[Stream[Coordinate]] = {
    List(coordSeqXY, coordSeqXYZ, coordSeqXYZM)
  }

  def fmemberOpts: List[Option[JsonObject]] = {
    List(None, Some(JsonObject("a" -> Json.fromString("b"))))
  }

  object Permutations {
    def points: List[Point] = {
      for {
        bbox <- bboxOpts
        coords <- coordSeqOpts
        fmember <- fmemberOpts
      } yield Point(coords.head, bbox, fmember)
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
