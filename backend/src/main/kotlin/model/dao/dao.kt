package model.dao

import model.tables.UserAuthTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import routes.auth.Role
import java.util.*

class UserAuth(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserAuth>(
        UsersTable
    )

    var username by UsersTable.username
    var hashPass by UsersTable.hashPass
    var role by UsersTable.role

    fun getRole(): Role = Role.valueOf(role)

}

