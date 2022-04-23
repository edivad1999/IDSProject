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
import model.tables.BillsTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import routes.auth.Role
import routes.auth.authenticate

fun Route.kitchenApi() = route("kitchen") {
    val db: Database by instance()
    authenticate(Role.KITCHEN) {
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
        post("editDishState") {
            val req = call.receive<EditStateRequest>()
            transaction(db) {
                DishEntity.findById(req.dishId.toUUID())!!.apply {
                    this.state = req.newState.name }
            }
            call.respond(HttpStatusCode.OK)
        }


    }
}
@Serializable
data class EditStateRequest(
    val dishId: String, val newState: DishState,
)