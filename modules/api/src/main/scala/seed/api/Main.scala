package seed.api

import seed.endpoints.TodoEndpoints
import seed.logic.TodoService
import seed.logic.db.DoobieTodoService
import sttp.tapir.AnyEndpoint
import sttp.tapir.model.ServerRequest
import sttp.tapir.redoc.bundle.RedocInterpreter
import sttp.tapir.server.interceptor.{DecodeFailureContext, DecodeSuccessContext, SecurityFailureContext, ServerResponseFromOutput}
import sttp.tapir.server.interceptor.log.ServerLog
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import sttp.tapir.ztapir._
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server}
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.config._
import zio.magic._
import zio.logging._
import zio.system._

object Main extends App {

  type Env = Logging with Has[TodoService]

  def layer: ZLayer[Any, Throwable, Env with zio.ZEnv] = {

    val config = System.live >>> ZConfig.fromSystemEnv(Config.descriptor)
    val todoService =
      ZLayer.wire[Has[TodoService]](config.project(_.db), Clock.live, Blocking.live, Transactors.layer, DoobieTodoService.layer)

    todoService ++ Slf4jLogging.env ++ ZEnv.live
  }

  type Logged[A] = RIO[Env, A]

  val serverLog: ServerLog[Logged] = new ServerLog[Logged] {
    def decodeFailureNotHandled(ctx: DecodeFailureContext): Logged[Unit] =
      ZIO.unit
    def decodeFailureHandled(ctx: DecodeFailureContext, response: ServerResponseFromOutput[_]): Logged[Unit] =
      Logging.warn(s"Decode failed handled: ${ctx}")
    def securityFailureHandled(ctx: SecurityFailureContext[Logged, _], response: ServerResponseFromOutput[_]): Logged[Unit] =
      Logging.warn(s"Security failed handled: ${ctx}")
    def requestHandled(ctx: DecodeSuccessContext[Logged, _, _], response: ServerResponseFromOutput[_]): Logged[Unit] =
      ZIO.unit
    def exception(e: AnyEndpoint, request: ServerRequest, ex: Throwable): Logged[Unit] =
      Logging.throwable(s"Error occurred on ${request.toString()}", ex)
  }

  val docs = RedocInterpreter()
    .fromEndpoints[Task](
      List(TodoEndpoints.list, TodoEndpoints.done, TodoEndpoints.insert),
      "ZIO seed - Todo example",
      "1.0"
    )


  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {

    val serverOpts = ZioHttpServerOptions.default[Env].copy(interceptors = ZioHttpServerOptions.customInterceptors.serverLog(serverLog).interceptors)
    val endpointHandlers = ZioHttpInterpreter(serverOpts).toHttp(TodosHandlers.all)
    val docHandlers = ZioHttpInterpreter().toHttp(docs)
    val server = Server.app(docHandlers ++ endpointHandlers) ++ Server.port(8080)

    server
      .make
      .provideLayer(layer ++ EventLoopGroup.auto() ++ ServerChannelFactory.auto)
      .useForever
      .exitCode
  }
}
