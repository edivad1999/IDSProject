package model.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import routes.auth.Role

object UserAuthTable : IntIdTable("users", "id") {
    val username = varchar("username", 50).uniqueIndex()
    val hashPass = varchar("password", 200)
    val role = varchar("role", 50).default(Role.USER.name)

}
