package app

import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir._
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.interop.catz._
import zio.logging._

object Main extends App {

  def countCharacters(s: String): ZIO[Logging, Nothing, Int] =
    ZIO.succeed(s.length)

  def namePerson(name: String) = ZIO.succeed(Person(name, 1337))

  def countEndpoint: ZServerEndpoint[Logging, ZioStreams] = Endpoints.countCharacters.zServerLogic(countCharacters)
  def nameEndpoint: ZServerEndpoint[Logging, ZioStreams] = Endpoints.namePerson.zServerLogic(namePerson)

  type Env = Logging

  type RuntimeEff[A] = RIO[Env with Clock with Blocking, A]

  def routes: HttpRoutes[RuntimeEff] =
    ZHttp4sServerInterpreter().from(List(countEndpoint.widen[Env], nameEndpoint.widen[Env])).toRoutes


  def endpoints = List(Endpoints.countCharacters, Endpoints.namePerson)

  def docs: HttpRoutes[RuntimeEff] = ZHttp4sServerInterpreter()
    .from(SwaggerInterpreter().fromEndpoints[RuntimeEff](endpoints, "ZIO seed", "1.0")).toRoutes

  def serve[R <: Clock with Blocking](routes: HttpRoutes[RIO[R, *]]): ZIO[R, Throwable, Unit] =
    ZIO.runtime[R].flatMap { implicit runtime =>
      BlazeServerBuilder[RIO[R, *]]
        .withExecutionContext(runtime.platform.executor.asEC)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(Router("/" -> routes).orNotFound)
        .withoutBanner
        .serve
        .compile
        .drain
    }

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    serve(routes <+> docs).exitCode.provideLayer(Slf4jLogging.env ++ ZEnv.live)
}
