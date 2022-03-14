package seed.domain.todoservice.doobie

import doobie._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.postgres.implicits._
import seed.core.Todo
import seed.logic.TodoService
import zio.{Has, Task, ZIO, ZLayer}
import zio.interop.catz._

import java.util.UUID

final case class DoobieTodoService(tx: Transactor[Task]) extends TodoService {

  object Queries {
    def insert(todo: Todo): Update0 =
      sql"insert into todos (id, description, done) values (${todo.id}, ${todo.description}, ${todo.done})".update

    def done(id: UUID): Update0 = sql"update todos set done = true where id = $id".update

    def list: Query0[Todo] = sql"select id, description, done from todos where done = false".query[Todo]
  }

  def insert(todo: Todo): Task[Int] = Queries.insert(todo).run.transact(tx)

  def done(id: UUID): Task[Int] = Queries.done(id).run.transact(tx)

  def list: Task[List[Todo]] = Queries.list.to[List].transact(tx)
}

object DoobieTodoService {
  val layer: ZLayer[Has[HikariTransactor[Task]], Nothing, Has[TodoService]] =
    ZIO.service[HikariTransactor[Task]].map(DoobieTodoService.apply).toLayer
}
