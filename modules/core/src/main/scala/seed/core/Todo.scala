package seed.core

import java.util.UUID

final case class Todo(id: UUID, description: String, done: Boolean)
