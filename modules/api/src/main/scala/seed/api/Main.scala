package seed.api

import cats.implicits._
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.{HttpApp, HttpRoutes}
import seed.endpoints.TodoEndpoints
import seed.logic.TodoService
import seed.logic.db.DoobieTodoService
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir._
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.config._
import zio.interop.catz._
import zio.logging._
import zio.system._

object Main extends App {

  type Env = Logging with Has[TodoService]
  type RuntimeEff[A] = RIO[Env with Clock with Blocking, A]

  def docs: HttpRoutes[RuntimeEff] =
    ZHttp4sServerInterpreter()
      .from(
        SwaggerInterpreter(basePrefix = List("api")).fromEndpoints[RuntimeEff](
            List(TodoEndpoints.list, TodoEndpoints.done, TodoEndpoints.insert),
            "ZIO seed - Todo example",
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

  def layer: ZLayer[Any, Throwable, Env with zio.ZEnv] = {

    val config = System.live >>> ZConfig.fromSystemEnv(Config.descriptor)
    val todoService = config.project(_.db) ++ Clock.live ++ Blocking.live >>> Transactors.layer >>> DoobieTodoService.layer

    todoService ++ Slf4jLogging.env ++ ZEnv.live
  }

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {

    val router =
      ZHttp4sServerInterpreter().from(TodosHandlers.all).toRoutes <+> docs

    def prg: Task[Unit] =
      (serve(router.orNotFound)).provideLayer(layer)

    prg.exitCode
  }
}
