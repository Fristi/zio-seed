package seed.core

sealed trait AppError

object AppError {
  case class Unexpected(error: Throwable) extends AppError
}
