package model.dataClasses

import routes.auth.Role

// TODO serialize entities to Data classes

data class User(
    val notes: String,
    val role: Role,
    val email: String,
    val billHistory: List<Bill>,
)

data class SimpleUser(
    //Abbiamo bisogno di due tipi di user altrimenti abbiamo una dipendenza ciclica
    val notes: String,
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
    val notes: String,
    val relatedClient: SimpleUser?,
    val menuElement: MenuElement,
)

data class MenuElement(
    val name: String,
    val ingredients: String,
    val description: String,
    val price: Float,
)
