val tapirVersion = "0.20.1"
val zioVersion = "1.0.13"
val doobieVersion = "1.0.0-RC2"

val domain = project.in(file("modules/domain"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion
    )
  )

val domainTodoService = project.in(file("modules/domain-todoservice"))
  .settings(commonSettings)
  .dependsOn(domain)
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio"       %% "zio-interop-cats" % "3.2.9.1",
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari" % doobieVersion
    )
  )

val endpoints = project.in(file("modules/endpoints"))
  .settings(commonSettings)
  .dependsOn(domain)
  .settings(
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-zio1" % tapirVersion
    )
  )

val api = project.in(file("modules/api"))
  .settings(commonSettings)
  .enablePlugins(NativeImagePlugin)
  .dependsOn(domainTodoService, endpoints)
  .settings(
    libraryDependencies ++= Seq(
      "io.github.kitlangton" %% "zio-magic" % "0.3.11",
      "com.outr" %% "scribe-slf4j" % "3.8.0",
      "dev.zio" %% "zio-config" % "2.0.0",
      "dev.zio" %% "zio-logging-slf4j" % "0.5.14",
      "org.scalameta" %% "svm-subs" % "20.2.0" % "compile-internal",
      "com.softwaremill.sttp.tapir"   %% "tapir-zio1-http-server"        % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-redoc-bundle" % tapirVersion
    ),
    nativeImageOptions ++= List(
      "--static",
      "--no-fallback",
      "--install-exit-handlers",
      "--enable-http",
      "--initialize-at-run-time=io.netty.channel.DefaultFileRegion",
      "--initialize-at-run-time=io.netty.channel.epoll.Native",
      "--initialize-at-run-time=io.netty.channel.epoll.Epoll",
      "--initialize-at-run-time=io.netty.channel.epoll.EpollEventLoop",
      "--initialize-at-run-time=io.netty.channel.epoll.EpollEventArray",
      "--initialize-at-run-time=io.netty.channel.kqueue.KQueue",
      "--initialize-at-run-time=io.netty.channel.kqueue.KQueueEventLoop",
      "--initialize-at-run-time=io.netty.channel.kqueue.KQueueEventArray",
      "--initialize-at-run-time=io.netty.channel.kqueue.Native",
      "--initialize-at-run-time=io.netty.channel.unix.Limits",
      "--initialize-at-run-time=io.netty.channel.unix.Errors",
      "--initialize-at-run-time=io.netty.channel.unix.IovArray",
      "--allow-incomplete-classpath",
      "-H:IncludeResources=META-INF/maven/org.webjars/.*|META-INF/resources/webjars/.*"
    ),
    nativeImageVersion := "20.2.0"
  )

lazy val seed = (project in file(".")).aggregate(domain, domainTodoService, endpoints, api)

def commonSettings = Seq(scalaVersion := "2.13.7")

def test(scope: String) = Seq(
  "dev.zio"                %% "zio-test"          % zioVersion            % scope,
  "dev.zio"                %% "zio-test-magnolia" % zioVersion            % scope,
  "dev.zio"                %% "zio-test-sbt"      % zioVersion            % scope
)
