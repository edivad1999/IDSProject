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
import model.dao.*
import model.dataClasses.Dish
import model.tables.BillsTable
import model.tables.TablesTable
import model.tables.UsersTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import routes.auth.*
import java.io.File
import java.time.Instant

fun Route.waitersApi() = route("waiter") {
    val db: Database by instance()
    val log: File by instance()

    authenticate(Role.WAITER) {

        get("billList") {
            call.respond(loggedTransaction(db, log, line =  LogLine(timestamp = Instant.now(), role = call.principal<BasePrincipal>()!!.role, username = call.principal<BasePrincipal>()!!.userId, operation = call.url { })) {
                BillEntity.all().map { it.serialize() }
            })
        }
        get("closeBill") {
            val billId = call.parameters["billId"]!!
            loggedTransaction(db, log, line =  LogLine(timestamp = Instant.now(), role = call.principal<BasePrincipal>()!!.role, username = call.principal<BasePrincipal>()!!.userId, operation = call.url { })) {
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
            call.respond(loggedTransaction(db, log, line =  LogLine(timestamp = Instant.now(), role = call.principal<BasePrincipal>()!!.role, username = call.principal<BasePrincipal>()!!.userId, operation = call.url { })) {
                val table = TableEntity.find { TablesTable.number eq tableNumber }.firstOrNull()!!
                val isAlreadyOccupied = BillEntity.find { BillsTable.relatedTable eq table.id }.any { it.closedAt == null } && !table.isOccupied
                if (!isAlreadyOccupied) {
                    table.isOccupied = true
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
            call.respond(loggedTransaction(db, log, line =  LogLine(timestamp = Instant.now(), role = call.principal<BasePrincipal>()!!.role, username = call.principal<BasePrincipal>()!!.userId, operation = call.url { })) {
                BillEntity.findById(billId.toUUID())!!.serialize()
            })
        }
        post("addToCourse") {
            val request = call.receive<AddToCourseWaiterRequest>()
            loggedTransaction(db, log, line =  LogLine(timestamp = Instant.now(), role = call.principal<BasePrincipal>()!!.role, username = call.principal<BasePrincipal>()!!.userId, operation = call.url { })) {
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
            call.respond(loggedTransaction(db, log, line =  LogLine(timestamp = Instant.now(), role = call.principal<BasePrincipal>()!!.role, username = call.principal<BasePrincipal>()!!.userId, operation = call.url { })) {
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
            loggedTransaction(db, log, line =  LogLine(timestamp = Instant.now(), role = call.principal<BasePrincipal>()!!.role, username = call.principal<BasePrincipal>()!!.userId, operation = call.url { })) {
                DishEntity.findById(dishId)!!.delete()
            }
            call.respond(HttpStatusCode.OK)
        }

        get("forceSetReady") {
            val courseId = call.parameters["courseId"]!!.toUUID()
            loggedTransaction(db, log, line =  LogLine(timestamp = Instant.now(), role = call.principal<BasePrincipal>()!!.role, username = call.principal<BasePrincipal>()!!.userId, operation = call.url { })) {
                CourseEntity.findById(courseId)!!.apply {
                    this.setReadyClients(this.getAllRelatedClients())
                    this.isSent = true
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
