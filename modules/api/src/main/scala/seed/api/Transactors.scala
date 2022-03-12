package seed.api

import cats.effect.std.Dispatcher
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import doobie.hikari.HikariTransactor
import zio.interop.catz._
import zio._
import zio.duration._
import zio.blocking.Blocking
import zio.clock.Clock

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

object Transactors {
  val layer : ZLayer[Clock with Blocking with Has[DbConfig], Throwable, Has[HikariTransactor[Task]]] = {
    def acquire(cfg: DbConfig) = ZIO.effect {
      val config = new HikariConfig()
      config.setPoolName("mycelium")
      config.setJdbcUrl(s"jdbc:postgresql://${cfg.host}:${cfg.port}/${cfg.database}")
      config.setUsername(cfg.username)
      config.setPassword(cfg.password)
      config.setValidationTimeout(1000)
      config.setConnectionTimeout(2000)
      config.setDriverClassName("org.postgresql.Driver")
      config.setMaximumPoolSize(10)
      new HikariDataSource(config)
    }

    ZLayer.fromManaged {
      for {
        cfg <- ZManaged.service[DbConfig]
        ds <- Managed.make(acquire(cfg))(ds => UIO(ds.close())).retry(Schedule.spaced(1.seconds) && Schedule.recurs(60))
        tx <- ZManaged.runtime[Clock with Blocking].flatMap { implicit rt: Runtime[Clock with Blocking] =>
          fixedThreadPool(10)
            .map(ce => HikariTransactor[Task](ds, ce))
        }
      } yield tx
    }

  }

  private def fixedThreadPool(maximumPoolSize: Int): ZManaged[Any, Throwable, ExecutionContextExecutor] =
    Managed.make(ZIO.effect(Executors.newFixedThreadPool(maximumPoolSize)))(es => UIO(es.shutdown())).map(ExecutionContext.fromExecutor)
}
