package model.dao

import model.tables.*
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import routes.auth.Role
import java.util.*

class UserEntity(uuid: EntityID<UUID>) : UUIDEntity(uuid) {
    companion object : UUIDEntityClass<UserEntity>(UsersTable)

    var username by UsersTable.username
    var hashPass by UsersTable.hashPass
    var role by UsersTable.role
    var email by UsersTable.email
    var billHistory by BillEntity via UsersBillsTable

    fun getRole(): Role = Role.valueOf(role)

    /**4
     * If uuid parameter is given it returns the bill that matches with the uuid and is not closed, if the uuid is not passed it returns the last bill that is still open.
     * if null is returned you may have not any open bill or any open bill that matches with param
     */
    fun getCurrentOpenBill(uuid: UUID?): BillEntity? =
        if (uuid != null) billHistory.firstOrNull { it.id.value == uuid && it.closedAt == null } else billHistory.sortedBy { it.openedAt }.firstOrNull { it.closedAt == null }


}

/**
 * C'è bisogno di questa entità perchè altrimenti si crea un ciclo con le referenze di course, dish e bill
 */
class SimpleUserEntity(uuid: EntityID<UUID>) : UUIDEntity(uuid) {
    companion object : UUIDEntityClass<SimpleUserEntity>(UsersTable)

    var username by UsersTable.username
    var hashPass by UsersTable.hashPass
    var role by UsersTable.role
    var email by UsersTable.email
    fun getRole(): Role = Role.valueOf(role)

}

class BillEntity(uuid: EntityID<UUID>) : UUIDEntity(uuid) {
    companion object : UUIDEntityClass<BillEntity>(BillsTable)

    var secretCode by BillsTable.secretCode
    var coveredNumbers by BillsTable.coveredNumbers
    var openedAt by BillsTable.openedAt
    var closedAt by BillsTable.closedAt
    var relatedTable by TableEntity referencedOn BillsTable.relatedTable
    var users by SimpleUserEntity via UsersBillsTable


}

class TableEntity(uuid: EntityID<UUID>) : UUIDEntity(uuid) {
    companion object : UUIDEntityClass<TableEntity>(TablesTable)

    var number by TablesTable.number
    var isOccupied by TablesTable.isOccupied
//    val billHistory by BillEntity referrersOn BillsTable.relatedTable crea un ciclo

}

class CourseEntity(uuid: EntityID<UUID>) : UUIDEntity(uuid) {

    companion object : UUIDEntityClass<CourseEntity>(CoursesTable)
    var isSent by CoursesTable.isSpedita
    var sentAt by CoursesTable.speditaAt
    private var readyClients by CoursesTable.readyClients //È privato perché ci si accede con le funzioni e non va mai toccato a mano
    val dishes by DishEntity referrersOn DishesTable.relatedCourse

    fun getReadyClients() = (readyClients?.split(",") ?: emptyList()).mapNotNull { SimpleUserEntity.findById(it.toUUID()) }
    fun setReadyClients(users: List<SimpleUserEntity>) {
        readyClients = users.map { it.id.value }.joinToString { "," }
    }

    fun setReadyOnlyOne(user: SimpleUserEntity) {
        val userAlreadyInside = getReadyClients().any { it.id == user.id }
        if (!userAlreadyInside) {
            setReadyClients(getReadyClients().toMutableList().apply { this.add(user) })
        }
    }
}

class DishEntity(uuid: EntityID<UUID>) : UUIDEntity(uuid) {
    companion object : UUIDEntityClass<DishEntity>(DishesTable)

    var notes by DishesTable.notes
    val relatedClient by SimpleUserEntity optionalBackReferencedOn DishesTable.relatedClient
    val menuElement by MenuElementEntity referencedOn DishesTable.menuElement


}

class MenuElementEntity(uuid: EntityID<UUID>) : UUIDEntity(uuid) {
    companion object : UUIDEntityClass<MenuElementEntity>(MenuElementTable)

    var name by MenuElementTable.name
    var ingredients by MenuElementTable.ingredients
    var description by MenuElementTable.description
    var price by MenuElementTable.price

}


// TODO Ho dimenticato stato piatto ovunque
enum class DishState{
    WAITING, PREPARING, DELIVERED, PROBLEM
}

fun String.toUUID(): UUID = UUID.fromString(this)
