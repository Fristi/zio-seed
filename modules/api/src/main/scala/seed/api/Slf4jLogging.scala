package seed.api

import scribe._
import scribe.filter._
import scribe.format.Formatter
import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger
import zio.{UIO, ULayer, ZLayer}

object Slf4jLogging {

  implicit class RichFilter(filter: Filter) {
    def &&(other: Filter): Filter = new Filter {
      override def matches[M](record: LogRecord[M]): Boolean = filter.matches(record) && other.matches(record)
    }
  }

  val env: ULayer[Logging] = {
    def setupScribe: Logger = {
      Logger.root
        .clearModifiers()
        .clearHandlers()
        .withHandler(formatter = Formatter.compact, minimumLevel = Some(Level.Info))
        .withModifier(exclude(packageName.startsWith("org.http4s.blaze") && level < Level.Warn))
        .replace()
    }

    ZLayer.fromEffect(UIO(setupScribe)) >>> Slf4jLogger.make((_, message) => "%s".format(message))
  }
}
