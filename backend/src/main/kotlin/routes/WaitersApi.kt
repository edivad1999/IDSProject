package routes

import instance
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import model.dao.*
import model.dataClasses.Dish
import model.tables.BillsTable
import model.tables.TablesTable
import model.tables.UsersTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import routes.auth.Role

fun Route.waitersApi() = route("waiter") {
    val db: Database by instance()
    authenticate(Role.WAITER.name) {

        get("billList") {
            call.respond(transaction(db) {
                BillEntity.all().map { it.serialize() }
            })
        }
        get("closeBill") {
            val billId = call.parameters["billId"]!!
            transaction(db) {
                BillEntity.findById(billId.toUUID())!!.apply {
                    closedAt = System.currentTimeMillis()
                    relatedTable.apply {
                        isOccupied = false
                    }

                }
            }
            call.respond(HttpStatusCode.OK)
        }
        get("openBill") {
            val tableNumber = call.parameters["tableNumber"]!!.toInt()
            val coveredNumber = call.parameters["coveredNumber"]!!.toInt()
            call.respond(transaction(db) {
                val table = TableEntity.find { TablesTable.number eq tableNumber }.firstOrNull()!!
                val isAlreadyOccupied = BillEntity.find { BillsTable.relatedTable eq table.id }.all { it.closedAt != null } && !table.isOccupied
                if (!isAlreadyOccupied) {
                    val code = BillEntity.new {
                        this.openedAt = System.currentTimeMillis()
                        this.relatedTable = table
                        this.coveredNumbers = coveredNumber
                        this.secretCode = ('0'..'9').shuffled().take(4).joinToString("")
                        this.closedAt = null
                    }.serialize().secretCode
                    SimpleStringResponse(code)
                } else HttpStatusCode.BadRequest

            }
            )
        }
        get("getBill") {
            val billId = call.parameters["billId"]!!
            call.respond(transaction(db) {
                BillEntity.findById(billId.toUUID())!!.serialize()
            })
        }
        post("addToCourse") {
            val request = call.receive<AddToCourseWaiterRequest>()
            transaction(db) {
                val course = CourseEntity.findById(request.courseId.toUUID()) ?: CourseEntity.new {
                    this.isSent = false
                    this.setReadyClients(emptyList())
                    this.relatedBillID = BillEntity.findById(request.billId.toUUID())!!.id
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
        /*Same as client*/
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
        /*Same as client*/
        get("removeDish") {
            val dishId = call.parameters["dishId"]!!.toUUID()
            transaction(db) {
                DishEntity.findById(dishId)!!.delete()
            }
            call.respond(HttpStatusCode.OK)
        }

        get("forceSetReady") {
            val courseId = call.parameters["courseId"]!!.toUUID()
            transaction(db) {
                CourseEntity.findById(courseId)!!.apply {
                    this.setReadyClients(this.getAllRelatedClients())
                }

            }
            call.respond(HttpStatusCode.OK)
        }


    }
}

data class SimpleStringResponse(val responseString: String)
data class AddToCourseWaiterRequest(
    val dish: Dish,
    val courseId: String,
    val billId: String,
)
