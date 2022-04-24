package model.dataClasses

import kotlinx.serialization.Serializable
import model.dao.DishState
import routes.auth.Role
import java.util.*

// TODO serialize entities to Data classes
@Serializable
data class User(
    val username: String,
    val role: Role,
    val email: String,
    val billHistory: List<Bill>,
)

@Serializable
data class SimpleUser(
    val username: String,
    val role: Role,
    val email: String,
)

@Serializable
data class Bill(
    val secretCode: String,
    val coveredNumbers: Int,
    val openedAt: Long,
    val closedAt: Long?,
    val relatedTable: Table,
    val users: List<SimpleUser>,
    val courses: List<Course>,
)

@Serializable
data class Table(
    val number: Int,
    val isOccupied: Boolean,
)

@Serializable
data class Course(
    val isSent: Boolean,
    val number: Int,
    val sentAt: Long?,
    val readyClients: List<SimpleUser>,
    val dishes: List<Dish>,
)

@Serializable
data class Dish(
    val uuid: String,
    val notes: String,
    val relatedClient: SimpleUser?,
    val menuElement: MenuElement,
    val state: DishState,
)

@Serializable
data class MenuElement(
    val name: String,
    val ingredients: String,
    val description: String,
    val price: Float,
    val uuid: String = UUID.randomUUID().toString()
)
