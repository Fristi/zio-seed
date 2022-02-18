package app

import zio._
import zio.logging._

object Main extends App {

  def hello: ZIO[Logging, Nothing, Unit] =
    Logging.info("Hello from slf4j")


  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    hello.provideLayer(Slf4jLogging.env).as(ExitCode.success)
}
