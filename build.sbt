val tapirVersion = "0.19.3"
val zioVersion = "1.0.13"

val core = project.in(file("modules/core"))
  .settings(commonSettings)

val logic = project.in(file("modules/logic"))
  .settings(commonSettings)
  .dependsOn(core)
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion
    )
  )

val endpoints = project.in(file("modules/endpoints"))
  .settings(commonSettings)
  .dependsOn(core)
  .settings(
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-zio" % tapirVersion
    )
  )

val api = project.in(file("modules/api"))
  .settings(commonSettings)
  .enablePlugins(NativeImagePlugin)
  .dependsOn(logic, endpoints)
  .settings(
    libraryDependencies ++= Seq(
      "com.outr" %% "scribe-slf4j" % "3.8.0",
      "dev.zio" %% "zio-config" % "1.0.10",
      "dev.zio" %% "zio-logging-slf4j" % "0.5.14",
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http4s-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion
    ),
    nativeImageOptions ++= List(
      "--no-fallback",
      "--allow-incomplete-classpath",
      "--enable-https",
      "-H:IncludeResources=META-INF/maven/org.webjars/.*|META-INF/resources/webjars/.*"
    ),
    nativeImageVersion := "21.3.0"
  )

lazy val seed = (project in file(".")).aggregate(core, logic, endpoints, api)

def commonSettings = Seq(
  scalaVersion := "2.13.8",
  addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)
)


