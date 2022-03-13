package seed.api

import sttp.tapir.AnyEndpoint
import sttp.tapir.model.ServerRequest
import sttp.tapir.server.interceptor.{DecodeFailureContext, DecodeSuccessContext, SecurityFailureContext, ServerResponseFromOutput}
import sttp.tapir.server.interceptor.log.ServerLog
import zio.ZIO
import zio.logging.Logging

object ZioHttpServerLog extends ServerLog[Logged] {
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
