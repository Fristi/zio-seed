package app

import sttp.tapir._

object Endpoints {
  val countCharacters: Endpoint[Unit, String, Unit, Int, Any] =
    endpoint.in(path[String]("word")).out(plainBody[Int])
}
