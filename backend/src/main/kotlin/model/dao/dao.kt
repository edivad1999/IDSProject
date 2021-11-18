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
        UserAuthTable
    )

    var username by UserAuthTable.username
    var hashPass by UserAuthTable.hashPass
    var role by UserAuthTable.role

    fun getRole(): Role = Role.valueOf(role)

}

