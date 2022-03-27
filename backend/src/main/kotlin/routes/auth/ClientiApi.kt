package routes.auth

import instance
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import model.dao.BillEntity
import model.dao.TableEntity
import model.dao.UserEntity
import model.dao.toUUID
import model.tables.TablesTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.clientsApi() = route("clienti") {
    val db: Database by instance()
    authenticate(Role.CLIENT.name) {

        get("getBill") {
            val id = call.principal<BasePrincipal>()!!.userId.toUUID()
            call.respond(transaction(db) {
                UserEntity.findById(id)!!.getCurrentOpenBill(null)?.serialize()
            } ?: HttpStatusCode.BadRequest)
        }
        get("joinTable") {
            val id = call.principal<BasePrincipal>()!!.userId.toUUID()
            val req = call.receive<BillJoinRequest>()
            val res = transaction(db) {
                val tableNumber = TableEntity.find { TablesTable.number eq req.tableNumber }.firstOrNull()!!.number
                val bill = BillEntity.all().firstOrNull { it.relatedTable.number == tableNumber }!!
                if (bill.addUser(id, req.secretCode)) HttpStatusCode.OK else HttpStatusCode.BadRequest //Non sono sicuro si comporti come previsto
            }
            call.respond(res)
        }
    }

}

data class BillJoinRequest(
    val tableNumber: Int,
    val secretCode: String,
)
