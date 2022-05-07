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
import model.tables.TablesTable
import model.tables.UsersTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import routes.auth.BasePrincipal
import routes.auth.Role
import routes.auth.authenticate

fun Route.waitersApi() = route("waiter") {
    val db: Database by instance()
    authenticate(Role.WAITER) {

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
                BillEntity.findById(request.billId.toUUID())!!.let { bill ->
                    val course = bill.courses.firstOrNull { it.number == request.courseNumber } ?: CourseEntity.new {
                        isSent = false
                        setReadyClients(emptyList())
                        number = request.courseNumber
                        relatedBillID = bill.id
                    }
                    DishEntity.new {
                        relatedClient = call.principal<BasePrincipal>()!!.userId.findUser()
                        menuElement = MenuElementEntity.findById(request.dish.menuElement.uuid.toUUID())!!
                        notes = request.dish.notes
                        state = DishState.WAITING.name
                        relatedCourseID = course.id
                    }
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
                    this.relatedClient = UserEntity.find { UsersTable.username eq newDish.relatedClient.username }.first()
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

@Serializable
data class SimpleStringResponse(val responseString: String)

@Serializable
data class AddToCourseWaiterRequest(
    val dish: Dish,
    val courseNumber: Int,
    val billId: String,
)
