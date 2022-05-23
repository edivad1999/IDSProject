package model.tables

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
    val relatedTable = reference("relatedTable", TablesTable)//Così si mappa la 1 a n di tavoli conti // se si decidesse di rendere opzionale la referenza si può mettere nullable
}

object UsersBillsTable : Table("users-bills") {
    val user = reference("user", UsersTable)
    val bill = reference("bill", BillsTable)
    override val primaryKey = PrimaryKey(user, bill)
}

object TablesTable : UUIDTable("tables", "uuid") {
    val number = integer("number").uniqueIndex()
    val isOccupied = bool("isOccupied")
}

object CoursesTable : UUIDTable("courses", "uuid") {
    val relatedBill = reference("relatedBill", BillsTable)
    val isSent = bool("isSpedita")
    val sentAt = long("speditaAt").nullable()
    val readyClients = varchar("readyClients", 37001)
    val number=integer("number")
    // stiamo settando un limite di circa 1000 massimi pronti  idealmente si potrebbe fare una table di mapping

}

object DishesTable : UUIDTable("dishes", "uuid") {
    val relatedCourse = reference("relatedCourse", CoursesTable)
    val relatedClient = reference("relatedClient", UsersTable)
    val notes = varchar("notes", 600)
    val menuElement = reference("menuElement", MenuElementTable)
    val state = varchar("state", 200)

}

object MenuElementTable : UUIDTable("menu_elements", "uuid") {
    val name = varchar("name", 600)
    val ingredients = varchar("ingredients", 600)
    val description = varchar("description", 600)
    val price = float("price")
    val isCurrentlyActive=bool("isCurrentlyActive")
}
