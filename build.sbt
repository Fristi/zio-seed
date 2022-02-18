ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

enablePlugins(NativeImagePlugin)

nativeImageOptions ++= List(
  "--no-fallback",
  "--allow-incomplete-classpath",
  "--enable-https",
)
nativeImageVersion := "21.3.0"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.36"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.36"
libraryDependencies += "dev.zio" %% "zio" % "1.0.12"
libraryDependencies += "dev.zio" %% "zio-streams" % "1.0.12"
libraryDependencies += "dev.zio" %% "zio-logging-slf4j" % "0.5.14"

//libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % "0.20.0-M10"

lazy val root = (project in file("."))
  .settings(
    name := "zio-seed"
  )
