import com.thedeanda.lorem.Lorem
import di.DIModules
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import model.dao.*
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
    install(WebSockets)
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
            registerApi()
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
//    generateMockDataDB()
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
        SchemaUtils.drop(MenuElementTable, TablesTable, UsersBillsTable, BillsTable, CoursesTable, DishesTable, UsersTable, inBatch = false)
    }
    transaction(db) {
        initDb()
        mockMenu()
        mockTables()
        mockUsers()
        mockData()
        mockDishes()
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
        ),
        MenuElement(
            name = "Gnocchetti speck e panna",
            ingredients = "Gnocchi di patate, speck, panna",
            description = "Golosissimi Gnocchetti speck e panna ${lorem.getWords(5)}",
            price = 12f
        ),
        MenuElement(
            name = "Tagliata di Manzo",
            ingredients = "Bisteccazza di manzo",
            description = "Golosissima Tagliata di manzo ${lorem.getWords(5)}",
            price = 18f
        ),
        MenuElement(
            name = "Paillard alla griglia",
            ingredients = "Paillard, contorno",
            description = "Golosissima Paillard alla griglia con contorno di patate ${lorem.getWords(5)}",
            price = 11f
        ),
        MenuElement(
            name = "Filetto al pepe verde",
            ingredients = "Filetto di manzo, pepe, verde",
            description = "Golosissimo Filetto al pepe verde ${lorem.getWords(5)}",
            price = 10f
        ),
        MenuElement(
            name = "Scaloppine ai funghi misti",
            ingredients = "Scaloppine, funghi, misti",
            description = "Golosissime Scaloppine ai funghi misti ${lorem.getWords(5)}",
            price = 11f
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
        username = "utente1Tavolo1"
        hashPass = digester.digest("password")
        role = Role.CLIENT.name
        email = lorem.email
    }
    UserEntity.new {
        username = "utente1Tavolo2"
        hashPass = digester.digest("password")
        role = Role.CLIENT.name
        email = lorem.email
    }
    UserEntity.new {
        username = "utente1Tavolo3"
        hashPass = digester.digest("password")
        role = Role.CLIENT.name
        email = lorem.email
    }
    UserEntity.new {
        username = "utente2Tavolo1"
        hashPass = digester.digest("password")
        role = Role.CLIENT.name
        email = lorem.email
    }
    UserEntity.new {
        username = "utente2Tavolo2"
        hashPass = digester.digest("password")
        role = Role.CLIENT.name
        email = lorem.email
    }
    UserEntity.new {
        username = "utente3Tavolo2"
        hashPass = digester.digest("password")
        role = Role.CLIENT.name
        email = lorem.email
    }
    UserEntity.new {
        username = "utente2Tavolo3"
        hashPass = digester.digest("password")
        role = Role.CLIENT.name
        email = lorem.email
    }
    UserEntity.new {
        username = "MauroIlCameriere"
        hashPass = digester.digest("password")
        role = Role.CLIENT.name
        email = lorem.email
    }
    UserEntity.new {
        username = "PeppeIlCameriere"
        hashPass = digester.digest("password")
        role = Role.WAITER.name
        email = lorem.email
    }
    UserEntity.new {
        username = "LoChef"
        hashPass = digester.digest("password")
        role = Role.KITCHEN.name
        email = lorem.email
    }
    UserEntity.new {
        username = "IlSousChef"
        hashPass = digester.digest("password")
        role = Role.KITCHEN.name
        email = lorem.email
    }

}

fun Application.mockData() {
    val bills = listOf(
        BillEntity.new {
            secretCode = "0000"
            coveredNumbers = 4
            openedAt = System.currentTimeMillis()
            relatedTable = TableEntity.find { TablesTable.number eq 1 }.first().apply { isOccupied = true }
            this.addUser(UserEntity.find { UsersTable.username eq "utente1Tavolo1" }.first().id.value, "0000")
            this.addUser(UserEntity.find { UsersTable.username eq "utente2Tavolo1" }.first().id.value, "0000")
        },
        BillEntity.new {
            secretCode = "0001"
            coveredNumbers = 3
            openedAt = System.currentTimeMillis()
            relatedTable = TableEntity.find { TablesTable.number eq 2 }.first().apply { isOccupied = true }
            this.addUser(UserEntity.find { UsersTable.username eq "utente1Tavolo2" }.first().id.value, "0001")
            this.addUser(UserEntity.find { UsersTable.username eq "utente2Tavolo2" }.first().id.value, "0001")
            this.addUser(UserEntity.find { UsersTable.username eq "utente3Tavolo2" }.first().id.value, "0001")
        },
        BillEntity.new {
            secretCode = "0002"
            coveredNumbers = 2
            openedAt = System.currentTimeMillis()
            relatedTable = TableEntity.find { TablesTable.number eq 3 }.first().apply { isOccupied = true }
            this.addUser(UserEntity.find { UsersTable.username eq "utente1Tavolo3" }.first().id.value, "0002")
            this.addUser(UserEntity.find { UsersTable.username eq "utente2Tavolo3" }.first().id.value, "0002")
        },
        BillEntity.new {
            secretCode = "0003"
            coveredNumbers = 6
            openedAt = System.currentTimeMillis()
            relatedTable = TableEntity.find { TablesTable.number eq 4 }.first().apply { isOccupied = true }

        },
        BillEntity.new {
            secretCode = "0004"
            coveredNumbers = 5
            openedAt = System.currentTimeMillis()
            closedAt = System.currentTimeMillis() + 1000
            relatedTable = TableEntity.find { TablesTable.number eq 5 }.first().apply { isOccupied = false }
            closedAt = System.currentTimeMillis() + 1000

        },
        BillEntity.new {
            secretCode = "0005"
            coveredNumbers = 3
            openedAt = System.currentTimeMillis()
            relatedTable = TableEntity.find { TablesTable.number eq 5 }.first().apply { isOccupied = true }

        }

    )
    bills.forEach {
        CourseEntity.new {
            relatedBillID = it.id
            number = 1
            isSent = true
            sentAt = System.currentTimeMillis()
        }
        CourseEntity.new {
            relatedBillID = it.id
            number = 2
            isSent = false

        }
        CourseEntity.new {
            relatedBillID = it.id
            number = 3
            isSent = true
            sentAt = System.currentTimeMillis()
        }
    }
}

fun Application.mockDishes() {
    val lorem: Lorem by instance()
    DishEntity.new {
        relatedClient = UserEntity.find { UsersTable.username eq "utente1Tavolo1" }.first()
        relatedCourseID = UserEntity.find { UsersTable.username eq "utente1Tavolo1" }.first().getCurrentOpenBill(null)!!.courses.filter { it.number == 1 }.first().id
        notes = lorem.getWords(7)
        menuElement = MenuElementEntity.find { MenuElementTable.name eq "Tagliatelle al Ragù" }.first()
        state = DishState.DELIVERED.name
    }
    DishEntity.new {
        relatedClient = UserEntity.find { UsersTable.username eq "utente2Tavolo1" }.first()
        relatedCourseID = UserEntity.find { UsersTable.username eq "utente2Tavolo1" }.first().getCurrentOpenBill(null)!!.courses.filter { it.number == 2 }.first().id
        notes = lorem.getWords(7)
        menuElement = MenuElementEntity.find { MenuElementTable.name eq "Tortellini in Brodo" }.first()
        state = DishState.DELIVERED.name
    }
    DishEntity.new {
        relatedClient = UserEntity.find { UsersTable.username eq "utente1Tavolo1" }.first()
        relatedCourseID = UserEntity.find { UsersTable.username eq "utente1Tavolo1" }.first().getCurrentOpenBill(null)!!.courses.filter { it.number == 2 }.first().id
        notes = lorem.getWords(7)
        menuElement = MenuElementEntity.find { MenuElementTable.name eq "Tortellini in Brodo" }.first()
        state = DishState.WAITING.name
    }

}

