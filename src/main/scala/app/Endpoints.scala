package app

import sttp.tapir._
import sttp.tapir.json.zio._
import sttp.tapir.generic.auto._
import zio.json._

object Endpoints {
  val countCharacters: Endpoint[Unit, String, Unit, Int, Any] =
    endpoint.in("count" / path[String]("word")).out(plainBody[Int])

  val namePerson: Endpoint[Unit, String, Unit, Person, Any] =
    endpoint.in("person" / path[String]("name")).out(jsonBody[Person])

  implicit def decoderPerson: JsonDecoder[Person] = DeriveJsonDecoder.gen[Person]
  implicit def encoderPerson: JsonEncoder[Person] = DeriveJsonEncoder.gen[Person]
}
