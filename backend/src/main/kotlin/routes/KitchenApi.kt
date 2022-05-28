package routes

import instance
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import model.dao.*
import model.dataClasses.Course
import model.tables.BillsTable
import model.tables.CoursesTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import routes.auth.*
import java.io.File
import java.time.Instant

fun Route.kitchenApi() = route("kitchen") {
    val db: Database by instance()
    val log: File by instance()

    authenticate(Role.KITCHEN) {
        get("courses") {
            call.respond(loggedTransaction(db, log, line =  LogLine(timestamp = Instant.now(), role = call.principal<BasePrincipal>()!!.role, username = call.principal<BasePrincipal>()!!.userId, operation = call.url { })) {
                CourseEntity.find { CoursesTable.isSent eq true }.map {
                    val bill = BillEntity.findById(it.relatedBillID)!!
                    KitchenCourse(
                        course = it.serialize(),
                        isBillClosed = bill.closedAt?.let { true } ?: false,
                        tableNumber = bill.relatedTable.number
                    )

                }.filter { it.course.dishes.isNotEmpty() }.sortedByDescending { it.course.sentAt }
            })
        }
        get("coursesByTable") {
            val tableId = call.parameters["tableId"]!!
            call.respond(loggedTransaction(db, log, line =  LogLine(timestamp = Instant.now(), role = call.principal<BasePrincipal>()!!.role, username = call.principal<BasePrincipal>()!!.userId, operation = call.url { })) {
                BillEntity.find { BillsTable.relatedTable eq tableId.toUUID() }.firstOrNull()!!.courses.map {
                    val bill = BillEntity.findById(it.relatedBillID)!!
                    KitchenCourse(
                        course = it.serialize(),
                        isBillClosed = bill.closedAt?.let { true } ?: false,
                        tableNumber = bill.relatedTable.number
                    )

                }.sortedByDescending { it.course.sentAt }
            })
        }
        get("openCourses") {
            call.respond(loggedTransaction(db, log, line =  LogLine(timestamp = Instant.now(), role = call.principal<BasePrincipal>()!!.role, username = call.principal<BasePrincipal>()!!.userId, operation = call.url { })) {
                CourseEntity.find {
                    CoursesTable.isSent eq true
                }.filter { it.dishes.map { it.serialize() }.any { dish -> dish.state < DishState.DELIVERED } }.map {
                    val bill = BillEntity.findById(it.relatedBillID)!!
                    KitchenCourse(
                        course = it.serialize(),
                        isBillClosed = bill.closedAt?.let { true } ?: false,
                        tableNumber = bill.relatedTable.number
                    )
                }.filter { it.course.dishes.isNotEmpty() }.sortedByDescending { it.course.sentAt }

            })
        }
        post("editDishState") {
            val req = call.receive<EditStateRequest>()
            call.respond(loggedTransaction(db, log, line =  LogLine(timestamp = Instant.now(), role = call.principal<BasePrincipal>()!!.role, username = call.principal<BasePrincipal>()!!.userId, operation = call.url { })) {
                DishEntity.findById(req.dishId.toUUID())!!.apply {
                    this.state = req.newState.name
                }.serialize()
            }
            )
        }

    }
}

@Serializable
data class KitchenCourse(
    val course: Course,
    val isBillClosed: Boolean,
    val tableNumber: Int,

    )

@Serializable
data class EditStateRequest(
    val dishId: String, val newState: DishState,
)
