import di.DIModules
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.serialization.*
import kotlinx.coroutines.launch
import model.dao.UserEntity
import model.tables.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.ktor.DIFeature
import org.kodein.di.ktor.di
import routes.auth.*
import routes.clientsApi
import routes.kitchenApi
import routes.managerApi
import routes.waitersApi


fun Application.managerModule() {

    install(DIFeature) {
        import(DIModules.database)
        import(DIModules.serialization)
        import(DIModules.security)

    }
    initDb()


    install(CORS) {
        anyHost()
        HttpMethod.DefaultMethods.forEach {
            method(it)
        }
        allowNonSimpleContentTypes = true
        allowHeaders { true }
        exposedHeaders.add("Content-Disposition")
//        allowSameOrigin=true
    }

    install(Authentication) {
        val credentialVerifierJWT: JWTCredentialsVerifier by di().instance()
        Role.values().forEach { role ->
            jwt(role) {
                verifier(JwtConfig.verifier)
                realm = "edivad99"
                validate {
                    credentialVerifierJWT.verify(it)
                }
            }
        }
    }



    install(ContentNegotiation) {
        json(di().direct.instance())
    }


    routing {
        route("api") {
            loginApi()
            verifierJWTApi()
            clientsApi()
            waitersApi()
            managerApi()
            kitchenApi()
        }
    }

}

fun Application.initDb() = launch {
    val db: Database by instance()
    val digester: PasswordDigester by instance()
    transaction(db) {
        SchemaUtils.createMissingTablesAndColumns(UsersTable)
        SchemaUtils.createMissingTablesAndColumns(BillsTable)
        SchemaUtils.createMissingTablesAndColumns(UsersBillsTable)
        SchemaUtils.createMissingTablesAndColumns(TablesTable)
        SchemaUtils.createMissingTablesAndColumns(CoursesTable)
        SchemaUtils.createMissingTablesAndColumns(DishesTable)
        SchemaUtils.createMissingTablesAndColumns(MenuElementTable)

        val admin = UserEntity.find {
            UsersTable.username eq "admin"
        }.takeIf { it.empty() }?.let {
            UserEntity.new {
                username = "admin"
                hashPass = digester.digest("password")
                role = Role.MANAGER.name
                email = "manager@app.app"
            }

        }
        val client = UserEntity.find {
            UsersTable.username eq "client"
        }.takeIf { it.empty() }?.let {
            UserEntity.new {
                username = "client"
                hashPass = digester.digest("password")
                role = Role.CLIENT.name
                email = "client@app.app"
            }

        }
        val kitchen = UserEntity.find {
            UsersTable.username eq "kitchen"
        }.takeIf { it.empty() }?.let {
            UserEntity.new {
                username = "kitchen"
                hashPass = digester.digest("password")
                role = Role.KITCHEN.name
                email = "kitchen@app.app"
            }

        }
        val waiter = UserEntity.find {
            UsersTable.username eq "waiter"
        }.takeIf { it.empty() }?.let{
            UserEntity.new {
                username = "waiter"
                hashPass = digester.digest("password")
                role = Role.WAITER.name
                email = "waiter@app.app"
            }

        }
    }
}

fun Authentication.Configuration.jwt(
    role: Role,
    configure: JWTAuthenticationProvider.Configuration.() -> Unit,
) = jwt(role.name, configure)
