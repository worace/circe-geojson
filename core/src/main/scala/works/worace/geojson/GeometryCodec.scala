package works.worace.geojson

import io.circe.Decoder
import io.circe.syntax._
import io.circe.generic.extras.auto._
import io.circe.generic.extras.semiauto._
import TypeDiscriminator._
import CoordinateCodec.implicits._
import BBoxCodec.implicits._

private object GeometryCodec extends GjCodec[Geometry] {
  object implicits {
    implicit val geometryEncoder = encoder
    implicit val geometryDecoder = decoder
  }
  private val base: Decoder[Geometry] = deriveConfiguredDecoder[Geometry]
  val decoder: Decoder[Geometry] = Decoder.instance { cursor =>
    decodeWithForeignMembers(cursor, base, (geom, fMembers) => {
      geom match {
        case geom: ForeignMembers[Geometry] => geom.withForeignMembers(fMembers)
        case other                          => other
      }
    })
  }
}

private object PointCodec extends GjCodec[Point] {
  object implicits {
    implicit val pointEncoder = encoder
    implicit val pointDecoder = decoder
  }
  private val base: Decoder[Point] = deriveConfiguredDecoder[Point]
  val decoder: Decoder[Point] = Decoder.instance { cursor =>
    decodeWithForeignMembers(cursor, base, (geom, fMembers) => {
      geom.withForeignMembers(fMembers)
    })
  }
}

private object LineStringCodec extends GjCodec[LineString] {
  object implicits {
    implicit val lineStringEncoder = encoder
    implicit val lineStringDecoder = decoder
  }
  private val base: Decoder[LineString] = deriveConfiguredDecoder[LineString]
  val decoder: Decoder[LineString] = Decoder.instance { cursor =>
    decodeWithForeignMembers(cursor, base, (geom, fMembers) => {
      geom.withForeignMembers(fMembers)
    })
  }
}

private object PolygonCodec extends GjCodec[Polygon] {
  object implicits {
    implicit val polyonEncoder = encoder
    implicit val polyonDecoder = decoder
  }
  private val base: Decoder[Polygon] = deriveConfiguredDecoder[Polygon]
  val decoder: Decoder[Polygon] = Decoder.instance { cursor =>
    decodeWithForeignMembers(cursor, base, (geom, fMembers) => {
      geom.withForeignMembers(fMembers)
    })
  }
}

private object MultiPointCodec extends GjCodec[MultiPoint] {
  object implicits {
    implicit val multiPointEncoder = encoder
    implicit val multiPointDecoder = decoder
  }
  private val base: Decoder[MultiPoint] = deriveConfiguredDecoder[MultiPoint]
  val decoder: Decoder[MultiPoint] = Decoder.instance { cursor =>
    decodeWithForeignMembers(cursor, base, (geom, fMembers) => {
      geom.withForeignMembers(fMembers)
    })
  }
}

private object MultiLineStringCodec extends GjCodec[MultiLineString] {
  object implicits {
    implicit val multiLineStringEncoder = encoder
    implicit val multiLineStringDecoder = decoder
  }
  private val base: Decoder[MultiLineString] = deriveConfiguredDecoder[MultiLineString]
  val decoder: Decoder[MultiLineString] = Decoder.instance { cursor =>
    decodeWithForeignMembers(cursor, base, (geom, fMembers) => {
      geom.withForeignMembers(fMembers)
    })
  }
}

private object MultiPolygonCodec extends GjCodec[MultiPolygon] {
  object implicits {
    implicit val multiPolygonEncoder = encoder
    implicit val multiPolygonDecoder = decoder
  }
  private val base: Decoder[MultiPolygon] = deriveConfiguredDecoder[MultiPolygon]
  val decoder: Decoder[MultiPolygon] = Decoder.instance { cursor =>
    decodeWithForeignMembers(cursor, base, (geom, fMembers) => {
      geom.withForeignMembers(fMembers)
    })
  }
}
