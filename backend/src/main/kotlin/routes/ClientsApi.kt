package routes

import instance
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.dao.*
import model.dataClasses.Dish
import model.tables.BillsTable
import model.tables.UsersTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import routes.auth.BasePrincipal
import routes.auth.Role
import routes.auth.authenticate

fun Route.clientsApi() = route("clients") {
    val db: Database by instance()
    authenticate(Role.CLIENT) {
        get("whoAmI") {
            call.respond(transaction(db) {
                val user = UserEntity.find { UsersTable.username eq call.principal<BasePrincipal>()!!.userId }.firstOrNull()
                if (user != null) {
                    SimpleStringResponse(user.role)
                } else HttpStatusCode.BadRequest
            })

        }
        get("user") {
            call.respond(transaction(db) {
                call.principal<BasePrincipal>()!!.userId.findUser().simpleSerialize()
            })

        }
        get("getBill") {
            val username = call.principal<BasePrincipal>()!!.userId
            val response = transaction(db) {
                UserEntity.find { UsersTable.username eq username }.firstOrNull()!!.getCurrentOpenBill(null)?.serialize()
            } ?: HttpStatusCode.BadRequest
            call.respond(response)
        }
        post("joinTable") {
            val req = call.receive<BillJoinRequest>()
            val res = transaction(db) {
                val id = UserEntity.find { UsersTable.username eq call.principal<BasePrincipal>()!!.userId }.first().id.value
                val bill = BillEntity.find {
                    BillsTable.closedAt.isNull()
                }.firstOrNull {
                    it.relatedTable.number == req.tableNumber && it.relatedTable.isOccupied && it.users.none { userEntity -> userEntity.id.value == id }
                }
                bill?.let {
                    if (it.addUser(id, req.secretCode)) HttpStatusCode.OK else HttpStatusCode.BadRequest
                } ?: HttpStatusCode.BadRequest
            }
            call.respond(res)
        }
        get("getMenu") {
            call.respond(transaction(db) {
                MenuElementEntity.all().filter { it.isCurrentlyActive }.map { it.serialize() }
            })
        }
        post("editDish") {
            val toEditDish = call.receive<EditDishRequest>()
            val newDish = toEditDish.editedDish
            call.respond(transaction(db) {
                DishEntity.findById(toEditDish.toEditId.toUUID())!!.apply {
                    this.menuElement = MenuElementEntity.findById(newDish.menuElement.uuid.toUUID())!!
                    this.notes = newDish.notes
                    this.relatedClient = UserEntity.find { UsersTable.username eq newDish.relatedClient.username }.first()
                    this.state = newDish.state.name
                }.serialize()
            })
        }
        post("addToCourse") {
            val request = call.receive<AddToCourseRequest>()
            val userId = call.principal<BasePrincipal>()!!.userId
            val res = transaction(db) {
                val user = userId.findUser()
                user.getCurrentOpenBill()!!.let { bill ->
                    val course = bill.courses.firstOrNull { it.number == request.courseNumber } ?: CourseEntity.new {
                        this.isSent = false
                        this.setReadyClients(emptyList())
                        this.number = request.courseNumber
                        this.relatedBillID = bill.id

                    }
                    DishEntity.new {
                        this.menuElement = MenuElementEntity.findById(request.dish.menuElement.uuid.toUUID())!!
                        this.notes = request.dish.notes
                        this.relatedClient = user
                        this.state = DishState.WAITING.name
                        this.relatedCourseID = course.id
                    }
                    course.relatedBillID.value
                }

            }
            call.respond(HttpStatusCode.OK)
        }
        get("removeDish") {
            val dishId = call.parameters["dishId"]!!.toUUID()
            transaction(db) {
                DishEntity.findById(dishId)!!.delete()
            }
            call.respond(HttpStatusCode.OK)
        }
        get("setReady") {
            val courseId = call.parameters["courseId"]!!.toUUID()
            transaction(db) {
                val user = call.principal<BasePrincipal>()!!.userId.findUser()

                CourseEntity.findById(courseId)!!.setReadyOnlyOne(user)
            }
            call.respond(HttpStatusCode.OK)
        }
    }
    route("ws") {
        webSocket("{billId}") {
            val billId: String by call.parameters
            val json: Json by instance()
            while (true) {
                delay(2000)

                val bill = transaction(db) {
                    BillEntity.findById(billId.toUUID())?.serialize()
                }
                if (bill?.users?.map { it.username }?.contains(call.principal<BasePrincipal>()?.userId) == true) {
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Order not of the user"))
                    return@webSocket
                }
                send(json.encodeToString(bill))

            }

        }
    }


}

@Serializable
data class BillJoinRequest(
    val tableNumber: Int,
    val secretCode: String,
)

@Serializable
data class EditDishRequest(
    val toEditId: String,
    val editedDish: Dish,
)

@Serializable
data class AddToCourseRequest(
    val dish: Dish,
    val courseNumber: Int,
)

fun String.findUser(): UserEntity {
    return UserEntity.find { UsersTable.username eq this@findUser }.first()
}
