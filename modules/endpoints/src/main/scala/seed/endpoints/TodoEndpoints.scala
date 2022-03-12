package seed.endpoints

import seed.core.{AppError, Todo}
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.zio._
import sttp.tapir.generic.auto._
import zio.json._

import java.util.UUID

object TodoEndpoints {
  val error = oneOf[AppError](
    oneOfVariant(StatusCode.InternalServerError, emptyOutput.map(_ => AppError.Unexpected(new Exception("Unexpected error")))(_ => ()))
  )

  def list: Endpoint[Unit, Unit, AppError, List[Todo], Any] =
    endpoint.in("todos").get.errorOut(error).out(jsonBody[List[Todo]])

  def insert: Endpoint[Unit, Todo, AppError, Unit, Any] =
    endpoint.in("todos").post.errorOut(error).in(jsonBody[Todo]).out(emptyOutput)

  def done: Endpoint[Unit, UUID, AppError, Unit, Any] =
    endpoint.in("todos" / path[UUID]("id")).put.errorOut(error).out(emptyOutput)

  implicit def decoderTodo: JsonDecoder[Todo] = DeriveJsonDecoder.gen[Todo]

  implicit def encoderTodo: JsonEncoder[Todo] = DeriveJsonEncoder.gen[Todo]

  implicit def decoderAppError: JsonDecoder[AppError] =
    JsonDecoder.string.map(str => AppError.Unexpected(new Throwable(str)))

  implicit def encoderAppError: JsonEncoder[AppError] =
    JsonEncoder.string.contramap {
      case AppError.Unexpected(err) => err.getMessage
    }
}
