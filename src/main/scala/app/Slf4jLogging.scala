package app

import zio.ULayer
import zio.logging.{LogAnnotation, Logging}
import zio.logging.slf4j.Slf4jLogger

object Slf4jLogging {
  val format = "%s"

  val env: ULayer[Logging] =
    Slf4jLogger.make{(context, message) =>
      format.format(message)
    }
}
