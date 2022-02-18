package app

import zio.ULayer
import zio.logging.{LogAnnotation, Logging}
import zio.logging.slf4j.Slf4jLogger

object Slf4jLogging {
  val format = "[correlation-id = %s] %s"

  val env: ULayer[Logging] =
    Slf4jLogger.make{(context, message) =>
      val correlationId = LogAnnotation.CorrelationId.render(
        context.get(LogAnnotation.CorrelationId)
      )
      format.format(correlationId, message)
    }
}
