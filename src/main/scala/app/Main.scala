package app

import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.ztapir._
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.interop.catz._
import zio.logging._

object Main extends App {

  def countCharacters(s: String): ZIO[Logging, Nothing, Int] =
    Logging.info(s"Counting $s") *> ZIO.succeed(s.length)

  val countEndpoint: ZServerEndpoint[Logging, ZioStreams] = Endpoints.countCharacters.zServerLogic(countCharacters)

  type Env = Logging

  val routes: HttpRoutes[ZIO[Env with Has[Clock.Service] with Has[Blocking.Service], Throwable, *]] =
    ZHttp4sServerInterpreter().from(List(countEndpoint.widen[Env])).toRoutes


  def serve[R <: Clock with Blocking](routes: HttpRoutes[RIO[R, *]]): ZIO[R, Throwable, Unit] =
    ZIO.runtime[R].flatMap { implicit runtime =>
      BlazeServerBuilder[RIO[R, *]]
        .withExecutionContext(runtime.platform.executor.asEC)
        .bindHttp(8080, "localhost")
        .withHttpApp(Router("/" -> routes).orNotFound)
        .serve
        .compile
        .drain
    }

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    serve(routes).exitCode.provideLayer(Slf4jLogging.env ++ ZEnv.live)
}
