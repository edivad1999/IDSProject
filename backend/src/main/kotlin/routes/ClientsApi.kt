package routes

import instance
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
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
                    it.relatedTable.number == req.tableNumber
                            && it.relatedTable.isOccupied &&
                            it.users.none { userEntity -> userEntity.id.value == id }
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
                    this.relatedClient = UserEntity.find { UsersTable.username eq newDish.relatedClient?.username!! }.firstOrNull()
                    this.state = newDish.state.name
                }.serialize()
            })
        }
        post("addToCourse") {
            val request = call.receive<AddToCourseRequest>()
            val userId = call.principal<BasePrincipal>()!!.userId
            transaction(db) {
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
            val courseId = call.parameters.get("courseId")!!.toUUID()
            val userId = call.principal<BasePrincipal>()!!.userId.toUUID()

            transaction(db) {
                CourseEntity.findById(courseId)!!.setReadyOnlyOne(UserEntity.findById(userId)!!)
            }
            call.respond(HttpStatusCode.OK)
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
