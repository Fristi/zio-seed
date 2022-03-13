package seed.api

import seed.endpoints.TodoEndpoints
import seed.logic.TodoService
import seed.logic.db.DoobieTodoService
import sttp.tapir.redoc.bundle.RedocInterpreter
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server}
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.config._
import zio.magic._
import zio.system._

object Main extends App {

  def layer: ZLayer[Any, Throwable, Env with zio.ZEnv] = {

    val config = System.live >>> ZConfig.fromSystemEnv(Config.descriptor)
    val todoService =
      ZLayer.wire[Has[TodoService]](config.project(_.db), Clock.live, Blocking.live, Transactors.layer, DoobieTodoService.layer)

    todoService ++ Slf4jLogging.env ++ ZEnv.live
  }

  val redoc = RedocInterpreter()
    .fromEndpoints[Task](
      List(TodoEndpoints.list, TodoEndpoints.done, TodoEndpoints.insert),
      "ZIO seed - Todo example",
      "1.0"
    )

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {

    val serverOpts = ZioHttpServerOptions.default[Env].copy(interceptors = ZioHttpServerOptions.customInterceptors.serverLog(ZioHttpServerLog).interceptors)
    val endpointHandlers = ZioHttpInterpreter(serverOpts).toHttp(TodosHandlers.all)
    val docHandlers = ZioHttpInterpreter().toHttp(redoc)
    val server = Server.app(docHandlers ++ endpointHandlers) ++ Server.port(8080)

    server
      .make
      .provideLayer(layer ++ EventLoopGroup.auto() ++ ServerChannelFactory.auto)
      .useForever
      .exitCode
  }
}
