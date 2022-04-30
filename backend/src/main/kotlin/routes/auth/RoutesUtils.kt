package routes.auth

import com.auth0.jwt.interfaces.JWTVerifier
import instance
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.datetime.Clock
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.Serializable
import org.kodein.di.instance
import org.kodein.di.ktor.di

@Serializable
data class ErrorMessageResponse(val error: String)

fun Route.authenticate(role: Role, optional: Boolean = false, build: Route.() -> Unit) =
    authenticate(role.name, optional = optional, build = build)



