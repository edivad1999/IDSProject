package routes.auth

import io.ktor.auth.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.time.Instant

@Serializable
data class ErrorMessageResponse(val error: String)

fun Route.authenticate(role: Role, optional: Boolean = false, build: Route.() -> Unit) =
    authenticate(role.name, optional = optional, build = build)

data class LogLine(
    val timestamp: Instant,
    val role: Role,
    val username: String,
    val operation: String,
    val severity: String = "Default",
) {
    override fun toString(): String {
        return "$timestamp | $role | $username | $operation | $severity | \n"
    }
}

inline fun <T> loggedTransaction(
    db: Database,
    logFile: File,
    line: LogLine,
    crossinline statement: Transaction.() -> T,
): T {
    logFile.appendText(line.toString())
    return transaction(db) {
        statement()
    }
}


