package routes.auth

import instance
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import model.dao.*
import model.tables.BillsTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.kitchenApi() = route("kitchen") {
    val db: Database by instance()
    authenticate(Role.KITCHEN.name) {
        get("courses") {
            call.respond(transaction(db) {
                CourseEntity.all().map { it.serialize() }
            })
        }
        get("coursesByTable") {
            val tableId = call.parameters["tableId"]!!
            call.respond(transaction(db) {
                BillEntity.find { BillsTable.relatedTable eq tableId.toUUID() }.firstOrNull()!!.courses.map { it.serialize() }
            })
        }
        get("openCourses") {
            call.respond(transaction(db) {
                CourseEntity.all().filter { it.dishes.any { dish -> dish.getState() < DishState.DELIVERED } }.map { courseEntity ->
                    courseEntity.serialize().copy(dishes = courseEntity.dishes.filter { dishEntity -> dishEntity.getState() < DishState.DELIVERED }.map { it.serialize() })
                }//Si potrbbe fare meglio
            })
        }
        get("editDishState") {
            val req = call.receive<EditStateRequest>()
            transaction(db) {
                DishEntity.findById(req.dishId.toUUID())!!.apply { this.state = req.newState.name }
            }
            call.respond(HttpStatusCode.OK)
        }


    }
}

data class EditStateRequest(
    val dishId: String, val newState: DishState,
)
