import com.thedeanda.lorem.LoremIpsum
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import model.dao.BillEntity
import model.dao.MenuElementEntity
import model.dao.TableEntity
import model.dataClasses.MenuElement
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import routes.BillJoinRequest
import routes.SimpleStringResponse
import routes.auth.*
import java.net.URI

fun <T> testWithApplicationContext(test: (Database) -> T): T {
    val database = if (System.getenv("DATABASE_URL") != null) {
        val dbUri = URI(System.getenv("DATABASE_URL"))

        val username: String = dbUri.userInfo.split(":")[0]
        val password: String = dbUri.userInfo.split(":")[1]
        val dbUrl = "jdbc:postgresql://" + dbUri.host + ':' + dbUri.port + dbUri.path
        Database.connect(dbUrl, driver = "org.postgresql.Driver", user = username, password = password)


    } else {
        Database.connect("jdbc:postgresql://localhost:5439/postgres", driver = "org.postgresql.Driver", user = "postgres", password = "postgres")
    }
    return test(database)

}

fun provideClient(authTokenResponseData: AuthTokenResponseData? = null) = HttpClient(CIO) {
    install(JsonFeature) {
        serializer = KotlinxSerializer()
        acceptContentTypes = listOf(ContentType.parse("application/json"))
    }
    authTokenResponseData?.let {
        defaultRequest {
            headers.append("Authorization", "Bearer ${authTokenResponseData.jwt}")
        }
    }
}

fun buildUrl(url: String) = testEndPoint + url

val testEndPoint = "http://localhost:42069/api"

fun createMockUser(username: String, password: String, email: String, role: Role): Boolean {
    val encoder = JavaBase64Encoder()
    return runBlocking {
        provideClient().post<HttpStatusCode>(buildUrl("/register")) {
            parameter("role", role.name)
            contentType(ContentType.Application.Json)
            body = RegisterRequest(encoder.encodeString(username), encoder.encodeString(email), encoder.encodeString(password))
        }.isSuccess()
    }

}

fun retrieveAuthToken(role: Role): AuthTokenResponseData {
    val encoder = JavaBase64Encoder()
    runCatching { createMockUser("test${role.name}", "test", "test${role.name}@test.test", role) }
    return runBlocking {
        provideClient().post(buildUrl("/login")) {
            contentType(ContentType.Application.Json)

            body = LoginRequestData(encoder.encodeString("test${role.name}"), encoder.encodeString("test"))
        }

    }
}


fun mockMenu(): List<MenuElementEntity> {

    val lorem = LoremIpsum.getInstance()
    return listOf(MenuElement(name = "Tagliatelle al Ragù", ingredients = "Tagliatella e ragù", description = "Golosissime Tagliatelle al ragù di bovino ${lorem.getWords(5)}", price = 10f),
        MenuElement(name = "Tortellini in Brodo", ingredients = "Uovo, macinato, brodo", description = "Golosissimi Tortellini in brodo ${lorem.getWords(5)}", price = 10f),
        MenuElement(name = "Gnocchetti speck e panna", ingredients = "Gnocchi di patate, speck, panna", description = "Golosissimi Gnocchetti speck e panna ${lorem.getWords(5)}", price = 12f),
        MenuElement(name = "Tagliata di Manzo", ingredients = "Bisteccazza di manzo", description = "Golosissima Tagliata di manzo ${lorem.getWords(5)}", price = 18f),
        MenuElement(name = "Paillard alla griglia", ingredients = "Paillard, contorno", description = "Golosissima Paillard alla griglia con contorno di patate ${lorem.getWords(5)}", price = 11f),
        MenuElement(name = "Filetto al pepe verde", ingredients = "Filetto di manzo, pepe, verde", description = "Golosissimo Filetto al pepe verde ${lorem.getWords(5)}", price = 10f),
        MenuElement(name = "Scaloppine ai funghi misti", ingredients = "Scaloppine, funghi, misti", description = "Golosissime Scaloppine ai funghi misti ${lorem.getWords(5)}", price = 11f)

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

fun mockTables() {
    (1..20).forEach {
        TableEntity.new {
            isOccupied = false
            number = it
        }
    }

}

fun getBillData(tokenWaiter: AuthTokenResponseData): BillJoinRequest? = runBlocking {
    runCatching {
        provideClient(tokenWaiter).get<SimpleStringResponse>(buildUrl("/waiter/openBill")) {
            contentType(ContentType.Application.Json)
            parameter("tableNumber", 2)
            parameter("coveredNumber", 3)
        }
    }.getOrNull()?.responseString
}?.let {
    BillJoinRequest(
        2, it
    )
}

fun getBillId(tokenWaiter: AuthTokenResponseData): String {
    val data = getBillData(tokenWaiter)
    val res = testWithApplicationContext { db ->
        transaction(db) {
            BillEntity.all().first { it.relatedTable.number == data?.tableNumber }.id.value.toString()
        }
    }
    return res
}
