package seed.api

import zio.config._
import ConfigDescriptor._

final case class DbConfig(
  host: String,
  port: Int,
  username: String,
  password: String,
  database: String,
  poolSize: Int
)

object DbConfig {
  val config: ConfigDescriptor[DbConfig] = (
    string("PG_HOST").default("localhost") zip
    int("PG_PORT").default(5432) zip
    string("PG_USER").default("todos") zip
    string("PG_PASS").default("todos") zip
    string("PG_DB").default("todos") zip
    int("PG_POOL_SIZE").default(10)
  ).to[DbConfig]
}

final case class Config(db: DbConfig)

object Config {
  val descriptor: ConfigDescriptor[Config] =
    DbConfig.config.to[Config]
}

