package routes.auth

import instance
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import model.dao.*
import model.dataClasses.Dish
import model.tables.TablesTable
import model.tables.UsersTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.clientsApi() = route("clients") {
    val db: Database by instance()
    authenticate(Role.CLIENT.name) {

        get("getBill") {
            val id = call.principal<BasePrincipal>()!!.userId.toUUID()
            call.respond(transaction(db) {
                UserEntity.findById(id)!!.getCurrentOpenBill(null)?.serialize()
            } ?: HttpStatusCode.BadRequest)
        }
        get("joinTable") {
            val id = call.principal<BasePrincipal>()!!.userId.toUUID()
            val req = call.receive<BillJoinRequest>()
            val res = transaction(db) {
                val tableNumber = TableEntity.find { TablesTable.number eq req.tableNumber }.firstOrNull()!!.number
                val bill = BillEntity.all().firstOrNull { it.relatedTable.number == tableNumber }!!
                if (bill.addUser(id, req.secretCode)) HttpStatusCode.OK else HttpStatusCode.BadRequest //Non sono sicuro si comporti come previsto
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
                    this.relatedClient = SimpleUserEntity.find { UsersTable.username eq newDish.relatedClient?.username!! }.firstOrNull()
                    this.state = newDish.state.name
                }.serialize()
            })
        }
        post("addToCourse") {
            val request = call.receive<AddToCourseRequest>()
            val userId = call.principal<BasePrincipal>()!!.userId.toUUID()
            transaction(db) {
                val course = CourseEntity.findById(request.courseId.toUUID()) ?: CourseEntity.new {
                    this.isSent = false
                    this.setReadyClients(emptyList())
                    this.relatedBillID = UserEntity.findById(userId)!!.getCurrentOpenBill(null)!!.billId
                }
                DishEntity.findById(request.dish.uuid.toUUID())?.let {
                    it.relatedCourseID = course.id
                } ?: DishEntity.new {
                    this.menuElement = MenuElementEntity.findById(request.dish.menuElement.uuid.toUUID())!!
                    this.notes = request.dish.notes
                    this.relatedClient = SimpleUserEntity.find { UsersTable.username eq request.dish.relatedClient?.username!! }.firstOrNull()
                    this.state = DishState.WAITING.name
                    this.relatedCourseID = course.id
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
                CourseEntity.findById(courseId)!!.setReadyOnlyOne(SimpleUserEntity.findById(userId)!!)
            }
            call.respond(HttpStatusCode.OK)
        }
    }

}

data class BillJoinRequest(
    val tableNumber: Int,
    val secretCode: String,
)

data class EditDishRequest(
    val toEditId: String,
    val editedDish: Dish,
)

data class AddToCourseRequest(
    val dish: Dish,
    val courseId: String,
)
