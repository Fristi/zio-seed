package app

import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.ztapir._
import zhttp.http._
import zhttp.service.Server
import zio._
import zio.logging._

object Main extends App {

  def countCharacters(s: String): ZIO[Logging, Nothing, Int] =
    Logging.info(s"Counting $s") *> ZIO.succeed(s.length)

  val server: Http[Logging, Throwable, Request, Response[Logging, Throwable]] =
    ZioHttpInterpreter().toHttp(Endpoints.countCharacters.zServerLogic(countCharacters))


  def hello: ZIO[Logging, Nothing, Unit] =
    Logging.info("Hello from slf4j")


  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    Server.start(8080, server.silent).provideLayer(Slf4jLogging.env).exitCode
}
