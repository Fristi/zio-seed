package seed.api

import seed.logic.TodoService
import zio.Has
import zio.logging.Logging

object Layers {

  type Env = Logging with Has[TodoService]

}
