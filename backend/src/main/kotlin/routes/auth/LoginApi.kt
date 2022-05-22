package routes.auth


import instance
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import model.dao.UserEntity
import model.tables.UsersTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction


@Serializable
data class LoginRequestData(val username: String, val password: String)

/**
 * Route for basic login with mail and password if all user exist and password is right -> positive result
 * otherwise->negative result
 * usage: Post on /login
 * body request: LoginRequestData (email, password)
 * positive result: token
 * negative result: 401, Unauthorized
 */
fun Route.loginApi() = route("login") {

    val db: Database by instance()
    val digester: PasswordDigester by instance()
    val b64: Base64Encoder by instance()

    post {
        val (email, password) = call.receive<LoginRequestData>().let { (e, p) ->
            b64.decodeString(e) to b64.decodeString(p)
        }
        var failureReason = "username not found"
        val token = transaction(db) {
            UserEntity.find { UsersTable.username eq email }
                .limit(1)
                .firstOrNull()
                ?.let {
                    if (it.hashPass == digester.digest(password)) {
                        JwtConfig.makeToken(it)
                    } else {
                        failureReason = "password did not match"
                        null
                    }
                }

        }
        if (token != null) {
            call.respond(HttpStatusCode.OK, token)
        } else {
            call.respond(HttpStatusCode.Unauthorized, ErrorMessageResponse(failureReason))
        }
    }

}

fun Route.registerApi() = route("register") {
    val db: Database by instance()
    val digester: PasswordDigester by instance()
    val b64: Base64Encoder by instance()

    post {
        val req = call.receive<RegisterRequest>()
        transaction(db) {
            UserEntity.new {
                this.role = Role.CLIENT.name
                this.email = b64.decodeString(req.mail)
                this.username = b64.decodeString(req.username)
                this.hashPass = digester.digest(b64.decodeString(req.password))
            }
        }
        call.respond(HttpStatusCode.OK)

    }
}

@Serializable
data class RegisterRequest(
    val username: String,
    val mail: String,
    val password: String,
)
