package routes

import instance
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import model.dao.MenuElementEntity
import model.dao.TableEntity
import model.dao.toUUID
import model.dataClasses.MenuElement
import model.tables.TablesTable
import org.jetbrains.exposed.sql.Database
import routes.auth.*
import java.io.File
import java.time.Instant

fun Route.managerApi() = route("manager") {
    val db: Database by instance()
    val log: File by instance()

    authenticate(Role.WAITER) {
        get("getTables") {
            call.respond(loggedTransaction(db,
                log,
                line = LogLine(timestamp = Instant.now(), role = call.principal<BasePrincipal>()!!.role, username = call.principal<BasePrincipal>()!!.userId, operation = call.url { })) {
                TableEntity.all().map { it.serialize() }
            })
        }
    }
    authenticate(Role.MANAGER) {
        get("log") {
            call.respond(log.readLines().takeLast(200))
        }

        post("setTables") {
            val req = call.receive<MaxTablesRequest>()
            loggedTransaction(db,
                log,
                line = LogLine(timestamp = Instant.now(), role = call.principal<BasePrincipal>()!!.role, username = call.principal<BasePrincipal>()!!.userId, operation = call.url { })) {
                (1..req.maxTables).forEach {
                    TableEntity.find { TablesTable.number eq it }.firstOrNull() ?: TableEntity.new {
                        isOccupied = false
                        number = it
                    }
                }
            }
            call.respond(HttpStatusCode.OK)
        }
        post("setMenu") {
            val req = call.receive<MenuRequest>()
            loggedTransaction(db,
                log,
                line = LogLine(timestamp = Instant.now(), role = call.principal<BasePrincipal>()!!.role, username = call.principal<BasePrincipal>()!!.userId, operation = call.url { })) {
                MenuElementEntity.all().forEach { it.isCurrentlyActive = false }
                req.menuElements.forEach {
                    val element = MenuElementEntity.findById(it.uuid.toUUID())
                    if (element != null) {
                        element.isCurrentlyActive = true
                    } else {
                        MenuElementEntity.new {
                            name = it.name
                            ingredients = it.ingredients
                            description = it.description
                            price = it.price
                            isCurrentlyActive = true
                        }
                    }
                }
            }
            call.respond(HttpStatusCode.OK)
        }
    }
}

@Serializable
data class MenuRequest(
    val menuElements: List<MenuElement>,
)

@Serializable
data class MaxTablesRequest(
    val maxTables: Int,
)
