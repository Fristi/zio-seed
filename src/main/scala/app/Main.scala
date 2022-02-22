package app

import cats.implicits._
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.{HttpApp, HttpRoutes}
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir._
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.interop.catz._
import zio.system._
import zio.config._
import zio.logging._

object Main extends App {

  def countCharacters(s: String): ZIO[Logging, Nothing, Int] =
    ZIO.succeed(s.length)

  def namePerson(name: String) = ZIO.succeed(Person(name, 1337))

  def countEndpoint: ZServerEndpoint[Logging, ZioStreams] = Endpoints.countCharacters.zServerLogic(countCharacters)
  def nameEndpoint: ZServerEndpoint[Logging, ZioStreams] = Endpoints.namePerson.zServerLogic(namePerson)

  type Env = Logging with Has[Config]
  type RuntimeEff[A] = RIO[Env with Clock with Blocking, A]

  def docs: HttpRoutes[RuntimeEff] =
    ZHttp4sServerInterpreter()
      .from(
        SwaggerInterpreter(basePrefix = List("api")).fromEndpoints[RuntimeEff](
            List(Endpoints.countCharacters, Endpoints.namePerson),
            "ZIO seed",
            "1.0"
          )
      )
      .toRoutes

  def serve[R <: Clock with Blocking](routes: HttpApp[RIO[R, *]]): ZIO[R, Throwable, Unit] =
    ZIO.runtime[R].flatMap { implicit runtime =>
      BlazeServerBuilder[RIO[R, *]]
        .withExecutionContext(runtime.platform.executor.asEC)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(routes)
        .withoutBanner
        .serve
        .compile
        .drain
    }

  def layer: ZLayer[Any, Throwable, Env with ZEnv] = {

    val config = System.live >>> ZConfig.fromSystemEnv(Config.descriptor)

    config ++ Slf4jLogging.env ++ ZEnv.live
  }

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {

    def boot: ZIO[Logging with Has[Config], Nothing, Unit] =
      ZIO.service[Config].flatMap(config => Logging.info(s"Started (version: ${config.version})"))

    val router =
      ZHttp4sServerInterpreter().from(List(countEndpoint.widen[Env], nameEndpoint.widen[Env])).toRoutes <+> docs

    def prg: Task[Unit] =
      (boot *> serve(router.orNotFound)).provideLayer(layer)

    prg.exitCode
  }
}
