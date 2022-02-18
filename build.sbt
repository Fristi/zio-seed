ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

enablePlugins(NativeImagePlugin)

nativeImageOptions ++= List(
  "--no-fallback",
  "--allow-incomplete-classpath",
  "--enable-https",
)
nativeImageVersion := "21.3.0"

lazy val root = (project in file("."))
  .settings(
    name := "zio-seed"
  )
