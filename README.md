## circe-geojson

![Build Status](https://github.com/worace/circe-geojson/workflows/CI/badge.svg)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/works.worace/circe-geojson-core_2.12.svg)](https://oss.sonatype.org/content/repositories/snapshots/works/worace/circe-geojson-core_2.12/)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/works.worace/circe-geojson-jts.12.svg)](https://oss.sonatype.org/content/repositories/snapshots/works/worace/circe-geojson-jts_2.12/)

A Scala-friendly GeoJSON ADT for encoding and decoding with Circe. Includes optional JTS conversions.

Work in progress!

### Publishing

https://docs.scala-lang.org/overviews/contributors/index.html

* [ ] Readme
  * [ ] Install instructions
  * [ ] Background / Description
* [x] License
* [ ] Maven Central / Sonatype
  * [x] Make account - https://central.sonatype.org/pages/ossrh-guide.html
  * [ ] Claim domain? - https://issues.sonatype.org/browse/OSSRH-56244
    * [x] Show ownership via TXT record
* [ ] CI / CD
  * [X] Testing: Github Actions (https://github.com/olafurpg/setup-scala)
  * [ ] Publishing: Github Actions (https://github.com/olafurpg/setup-scala)
    * [ ] https://github.com/olafurpg/sbt-ci-release
    * [ ] Configure Credentials in GitHub Action: - https://help.github.com/en/actions/configuring-and-managing-workflows/creating-and-storing-encrypted-secrets
      * (Add them under settings > secrets in the github repo menu)
      * [x] sonatype user token - https://github.com/olafurpg/sbt-ci-release#optional-create-user-tokens
      * [ ] gpg pubring.asc
      * [ ] gpg secring.asc
* [ ] Docs
  * [ ] Readme Usage examples (tut/md - compile-time check)
  * [ ] Scaladoc
  * [ ] Publish scaladoc to github pages somehow?
