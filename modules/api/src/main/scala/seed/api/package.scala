package seed

import seed.core.AppError
import seed.logic.TodoService
import sttp.tapir.Endpoint
import sttp.tapir.ztapir.ZServerEndpoint
import sttp.tapir.ztapir._
import zio.{Has, IO, RIO, ZIO}
import zio.logging.Logging

package object api {
  type Logged[A] = RIO[Env, A]
  type Env = Logging with Has[TodoService]

  implicit class RichEndpoint[S, I, E, O, C](e: Endpoint[S, I, AppError, O, C]) {
    def handle[R >: Env <: Logging](logic: I => ZIO[R, Throwable, O])(implicit aIsUnit: S =:= Unit): ZServerEndpoint[R, C] =
      e.zServerLogic { input =>
        logic(input).tapError(err => Logging.throwable("Exception occurred", err)).either.flatMap {
          case Left(err) => IO.fail(AppError.Unexpected(err))
          case Right(value) => IO.succeed(value)
        }
      }
  }
}
