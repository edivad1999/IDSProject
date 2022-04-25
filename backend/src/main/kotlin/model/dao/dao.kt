package model.dao

import model.dataClasses.*
import model.tables.*
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.insert
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
    fun getCurrentOpenBill(uuid: UUID? = null): BillEntity? =
        if (uuid != null) billHistory.firstOrNull { it.id.value == uuid && it.closedAt == null } else billHistory.sortedByDescending { it.openedAt }.firstOrNull { it.closedAt == null }


    fun serialize() = User(username = this.username, role = this.getRole(), email = this.email, this.billHistory.toList().map { it.serialize() })
    fun simpleSerialize() = SimpleUser(username = this.username, role = this.getRole(), email = this.email)

}

/**
 * C'è bisogno di questa entità perchè altrimenti si crea un ciclo con le referenze di course, dish e bill
 */

class BillEntity(uuid: EntityID<UUID>) : UUIDEntity(uuid) {
    companion object : UUIDEntityClass<BillEntity>(BillsTable)

    var secretCode by BillsTable.secretCode
    var coveredNumbers by BillsTable.coveredNumbers
    var openedAt by BillsTable.openedAt
    var closedAt by BillsTable.closedAt
    var relatedTable by TableEntity referencedOn BillsTable.relatedTable
    val users by UserEntity via UsersBillsTable
    val courses by CourseEntity referrersOn CoursesTable.relatedBill
    fun serialize() = Bill(secretCode = secretCode,
        coveredNumbers = coveredNumbers,
        openedAt = openedAt,
        closedAt = closedAt,
        relatedTable = this.relatedTable.serialize(),
        users = users.map { it.simpleSerialize() },
        courses = courses.map { it.serialize() })

    fun addUser(userId: UUID, code: String) = UserEntity.findById(userId)!!.let { user ->
        if (secretCode == code && users.toList().size < coveredNumbers) {
            UsersBillsTable.insert {
                it[UsersBillsTable.user] = user.id
                it[UsersBillsTable.bill] = this@BillEntity.id.value
            }
            true
        } else false
    }


}


class TableEntity(uuid: EntityID<UUID>) : UUIDEntity(uuid) {
    companion object : UUIDEntityClass<TableEntity>(TablesTable)

    var number by TablesTable.number
    var isOccupied by TablesTable.isOccupied

    //    val billHistory by BillEntity referrersOn BillsTable.relatedTable crea un ciclo
    fun serialize() = Table(number = number, isOccupied = isOccupied)
}

class CourseEntity(uuid: EntityID<UUID>) : UUIDEntity(uuid) {

    companion object : UUIDEntityClass<CourseEntity>(CoursesTable)

    var relatedBillID by CoursesTable.relatedBill
    var isSent by CoursesTable.isSent
    var sentAt by CoursesTable.sentAt
    var number by CoursesTable.number
    private var readyClients by CoursesTable.readyClients //È privato perché ci si accede con le funzioni e non va mai toccato a mano
    val dishes by DishEntity referrersOn DishesTable.relatedCourse

    private fun getReadyClients() = readyClients?.takeIf { it.isNotEmpty() }?.split(",")?.map { UserEntity.findById(it.toUUID())!! } ?: emptyList()
    fun setReadyClients(users: List<UserEntity>) {
        readyClients = users.map { it.id.value }.joinToString { "," }
    }

    fun getAllRelatedClients(): List<UserEntity> {
        val billEntity = BillEntity.findById(relatedBillID)!!
        return billEntity.users.toList()

    }

    fun setReadyOnlyOne(user: UserEntity) {
        val userAlreadyInside = getReadyClients().any { it.id == user.id }
        if (!userAlreadyInside) {
            setReadyClients(getReadyClients().toMutableList().apply { this.add(user) })
        }
    }


    fun serialize() = Course(number = number, isSent = isSent, sentAt = sentAt, readyClients = getReadyClients().map { it.simpleSerialize() }, dishes = dishes.map { it.serialize() })
}

class DishEntity(uuid: EntityID<UUID>) : UUIDEntity(uuid) {
    companion object : UUIDEntityClass<DishEntity>(DishesTable)

    var notes by DishesTable.notes
    var relatedClient by UserEntity optionalReferencedOn DishesTable.relatedClient
    var menuElement by MenuElementEntity referencedOn DishesTable.menuElement
    var state by DishesTable.state
    var relatedCourseID by DishesTable.relatedCourse

    fun getRelatedCourse() = CourseEntity.findById(this.relatedCourseID)


    fun getState() = DishState.valueOf(state)
    fun serialize() = Dish(uuid = id.value.toString(), notes = notes, relatedClient = relatedClient?.simpleSerialize(), menuElement = menuElement.serialize(), state = getState())

}

class MenuElementEntity(uuid: EntityID<UUID>) : UUIDEntity(uuid) {
    companion object : UUIDEntityClass<MenuElementEntity>(MenuElementTable)

    var name by MenuElementTable.name
    var ingredients by MenuElementTable.ingredients
    var description by MenuElementTable.description
    var price by MenuElementTable.price
    var isCurrentlyActive by MenuElementTable.isCurrentlyActive
    fun serialize() = MenuElement(uuid = id.value.toString(), name = name, ingredients = ingredients, description = description, price = price)

}


enum class DishState {
    WAITING, PREPARING, DELIVERED, PROBLEM
}

fun String.toUUID(): UUID = UUID.fromString(this)
