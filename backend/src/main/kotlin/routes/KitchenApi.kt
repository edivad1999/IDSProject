package routes

import instance
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import model.dao.*
import model.dataClasses.Course
import model.tables.BillsTable
import model.tables.CoursesTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import routes.auth.Role
import routes.auth.authenticate

fun Route.kitchenApi() = route("kitchen") {
    val db: Database by instance()
    authenticate(Role.KITCHEN) {
        get("courses") {
            call.respond(transaction(db) {
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
            call.respond(transaction(db) {
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
            call.respond(transaction(db) {
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
            call.respond(transaction(db) {
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
