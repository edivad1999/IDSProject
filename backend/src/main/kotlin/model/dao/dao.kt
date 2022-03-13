package model.dao

import model.tables.BillsTable
import model.tables.TablesTable
import model.tables.UsersBillsTable
import model.tables.UsersTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import routes.auth.Role
import java.util.*

class User(uuid: EntityID<UUID>) : UUIDEntity(uuid) {
    companion object : UUIDEntityClass<User>(UsersTable)

    var username by UsersTable.username
    var hashPass by UsersTable.hashPass
    var role by UsersTable.role
    var email by UsersTable.email
    var billHistory by Bill via UsersBillsTable

    fun getRole(): Role = Role.valueOf(role)

    /**4
     * If uuid parameter is given it returns the bill that matches with the uuid and is not closed, if the uuid is not passed it returns the last bill that is still open.
     * if null is returned you may have not any open bill or any open bill that matches with param
     */
    fun getCurrentOpenBill(uuid: UUID?): Bill? =
        if (uuid != null) billHistory.filter { it.id.value == uuid && it.closedAt == null }.firstOrNull() else billHistory.sortedBy { it.openedAt }.firstOrNull { it.closedAt == null }


}

class Bill(uuid: EntityID<UUID>) : UUIDEntity(uuid) {
    companion object : UUIDEntityClass<Bill>(BillsTable)

    var secretCode by BillsTable.secretCode
    var coveredNumbers by BillsTable.coveredNumbers
    var openedAt by BillsTable.openedAt
    var closedAt by BillsTable.closedAt
    var relatedTable = BillsTable.reference("relatedTable", TablesTable)
    var users by User via UsersBillsTable


}

class Table(uuid: EntityID<UUID>) : UUIDEntity(uuid) {
    companion object : UUIDEntityClass<Table>(TablesTable)

    var number by TablesTable.number
    var isOccupied by TablesTable.isOccupied
    val billHistory by Bill referrersOn BillsTable.relatedTable

}

