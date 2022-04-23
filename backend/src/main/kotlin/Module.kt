import com.thedeanda.lorem.Lorem
import di.DIModules
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.serialization.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import model.dao.MenuElementEntity
import model.dao.TableEntity
import model.dao.UserEntity
import model.dataClasses.MenuElement
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
    init()

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

fun Application.init() = launch {
    initDb()
    generateMockDataDB()
}


fun Application.initDb() = runBlocking {
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
        }.takeIf { it.empty() }?.let {
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


fun Application.generateMockDataDB() = runBlocking {
    val db: Database by instance()
    transaction(db) {
        SchemaUtils.drop(MenuElementTable, TablesTable, UsersBillsTable, BillsTable, CoursesTable, DishesTable, UsersTable, inBatch = true)
        initDb()
        mockMenu()
        mockTables()

//        initDb()
    }

}

fun Application.mockMenu(): List<MenuElementEntity> {
    val lorem: Lorem by instance()
    return listOf(
        MenuElement(
            name = "Tagliatelle al Ragù",
            ingredients = "Tagliatella e ragù",
            description = "Golosissime Tagliatelle al ragù di bovino ${lorem.getWords(5)}",
            price = 10f
        ),
        MenuElement(
            name = "Tortellini in Brodo",
            ingredients = "Uovo, macinato, brodo",
            description = "Golosissimi Tortellini in brodo ${lorem.getWords(5)}",
            price = 10f
        )

    ).map {
        MenuElementEntity.new {
            name = it.name
            ingredients = it.ingredients
            price = it.price
            description = it.description
            isCurrentlyActive = true
        }
    }
}

fun Application.mockTables() {
    (1..20).forEach {
        TableEntity.new {
            isOccupied = false
            number = it
        }
    }
}

fun Application.mockUsers() {
    val digester: PasswordDigester by instance()
    val lorem: Lorem by instance()
    UserEntity.new {
        username = "utenteTavolo1"
        hashPass = digester.digest("password")
        role = Role.WAITER.name
        email = lorem.email
    }
}

