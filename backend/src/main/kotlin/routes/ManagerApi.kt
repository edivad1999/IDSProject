package routes

import instance
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import model.dao.MenuElementEntity
import model.dao.TableEntity
import model.dao.toUUID
import model.dataClasses.MenuElement
import model.tables.TablesTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import routes.auth.Role
import routes.auth.authenticate

fun Route.managerApi() = route("manager") {
    val db: Database by instance()
    authenticate(Role.MANAGER) {
        get("getTables") {
            call.respond(transaction(db) {
                TableEntity.all().map { it.serialize() }
            })
        }
        post("setTables") {
            val req = call.receive<MaxTablesRequest>()
            transaction(db) {
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
            transaction(db) {
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
