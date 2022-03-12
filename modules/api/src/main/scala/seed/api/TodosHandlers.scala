package seed.api

import seed.api.Layers.Env
import seed.core.AppError
import seed.endpoints.TodoEndpoints
import seed.logic.TodoService
import sttp.tapir.ztapir._
import zio.{IO, RIO, ZIO}

object TodosHandlers {

  val insert =
    TodoEndpoints
      .insert
      .zServerLogic(todo => handle(TodoService(_.insert(todo)).unit))
      .widen[Env]

  val list =
    TodoEndpoints
      .list
      .zServerLogic(_ => handle(TodoService(_.list)))
      .widen[Env]

  val done =
    TodoEndpoints
      .done
      .zServerLogic(id => handle(TodoService(_.done(id))).unit)
      .widen[Env]

  def handle[R, A](prg: RIO[R, A]): ZIO[R, AppError, A] =
    prg.either.flatMap {
      case Left(err) => IO.fail(AppError.Unexpected(err))
      case Right(value) => IO.succeed(value)
    }

  val all =
    List(insert, list, done)

}
