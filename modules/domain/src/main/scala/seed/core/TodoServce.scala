package seed.logic

import seed.core.Todo
import zio.{Accessible, Task}

import java.util.UUID

trait TodoService {
  def insert(todo: Todo): Task[Int]
  def done(id: UUID): Task[Int]
  def list: Task[List[Todo]]
}

object TodoService extends Accessible[TodoService]
