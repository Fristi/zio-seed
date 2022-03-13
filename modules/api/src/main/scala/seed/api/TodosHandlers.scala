package seed.api

import seed.endpoints.TodoEndpoints
import seed.logic.TodoService

object TodosHandlers {

  val insert =
    TodoEndpoints
      .insert
      .handle(todo => TodoService(_.insert(todo)).unit)

  val list =
    TodoEndpoints
      .list
      .handle(_ => TodoService(_.list))

  val done =
    TodoEndpoints
      .done
      .handle(id => TodoService(_.done(id)).unit)

  val all =
    List(insert, list, done)

}
