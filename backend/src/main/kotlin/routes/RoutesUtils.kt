package routes

import io.ktor.routing.*
import kotlinx.serialization.Serializable
import routes.auth.Role
import io.ktor.auth.*

@Serializable
data class ErrorMessageResponse(val error: String)

fun Route.authenticate(role: Role, optional: Boolean = false, build: Route.() -> Unit) =
    authenticate(role.name, optional = optional, build = build)
