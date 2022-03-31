package model.dataClasses

import model.dao.DishState
import routes.auth.Role
import java.util.UUID

// TODO serialize entities to Data classes

data class User(
    val username: String,
    val role: Role,
    val email: String,
    val billHistory: List<Bill>,
)

data class SimpleUser(
    //Abbiamo bisogno di due tipi di user altrimenti abbiamo una dipendenza ciclica
    val username: String,
    val role: Role,
    val email: String,
)

data class Bill(
    val secretCode: String,
    val coveredNumbers: Int,
    val openedAt: Long,
    val closedAt: Long?,
    val relatedTable: Table,
    val users: List<SimpleUser>,
    val courses:List<Course>
)

data class Table(
    val number: Int,
    val isOccupied: Boolean,
)

data class Course(
    val isSent: Boolean,
    val sentAt: Long?,
    val readyClients: List<SimpleUser>,
    val dishes: List<Dish>,
)


data class Dish(
    val uuid: String,
    val notes: String,
    val relatedClient: SimpleUser?,
    val menuElement: MenuElement,
    val state:DishState
)

data class MenuElement(
    val uuid: String,
    val name: String,
    val ingredients: String,
    val description: String,
    val price: Float,
)
