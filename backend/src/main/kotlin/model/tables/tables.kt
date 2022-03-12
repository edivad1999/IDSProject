package model.tables

import model.tables.BillsTable.nullable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Table
import routes.auth.Role

/*
* Qua si definisce la logica delle tabelle del db, ancora non si parla di classi
*/

object UsersTable : UUIDTable("users", "uuid") {
    val username = varchar("username", 50).uniqueIndex()
    val hashPass = varchar("password", 200)
    val role = varchar("role", 50).default(Role.CLIENT.name)
    val email = varchar("email", 50).uniqueIndex()
}

object BillsTable : UUIDTable("bills", "uuid") {
    val secretCode = varchar("secretCode", 200)
    val coveredNumbers = integer("coveredNumbers")
    val openedAt = long("openedAt")
    val closedAt = long("closedAt").nullable()
    val associatedTable = reference("associatedTable", TablesTable) //Così si mappa la 1 a n di tavoli conti // se si decidesse di rendere opzionale la referenza si può mettere nullable
}

object UsersBillsTable : Table("users-bills") {
    private val user = reference("user", UsersTable)
    private val bill = reference("bill", BillsTable)
    override val primaryKey = PrimaryKey(user, bill)
}

object TablesTable : UUIDTable("tables", "uuid") {
    val number = integer("number")
    val isOccupied = bool("number")
}

object CoursesTable : UUIDTable("courses", "uuid") {
    val associatedBill = reference("associatedBill", BillsTable)
    val isSpedita=bool("isSpedita")
    val speditaAt = long("speditaAt").nullable()
    val readyClients = varchar("readyClients",37001 )
        // stiamo settando un limite di circa 1000 massimi pronti  idealmente si potrebbe fare una table di mapping

}
