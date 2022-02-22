ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

enablePlugins(NativeImagePlugin)

nativeImageOptions ++= List(
  "--no-fallback",
  "--allow-incomplete-classpath",
  "--enable-https",
  "-H:IncludeResources=logback.xml|META-INF/resources/webjars/.*"
)
nativeImageVersion := "21.3.0"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.36"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.36"
libraryDependencies += "dev.zio" %% "zio" % "1.0.12"
libraryDependencies += "dev.zio" %% "zio-streams" % "1.0.12"
libraryDependencies += "dev.zio" %% "zio-logging-slf4j" % "0.5.14"

libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-zio-http4s-server" % "0.19.3"
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-json-zio" % "0.19.3"
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % "0.19.3"

addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)

lazy val root = (project in file("."))
  .settings(
    name := "zio-seed"
  )
