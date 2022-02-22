package app

import zio.config._, ConfigDescriptor._

final case class Config(version: String)

object Config {
  val descriptor: ConfigDescriptor[Config] =
    string("VERSION").default("1.0.0").to[Config]
}
