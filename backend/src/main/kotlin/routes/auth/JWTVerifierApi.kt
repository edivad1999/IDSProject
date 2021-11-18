package routes.auth

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import routes.authenticate

fun Route.verifierJWTApi() = route("verifyToken") {

    authenticate(Role.USER) {
        get {
            call.respond(HttpStatusCode.OK)
        }
    }
}


