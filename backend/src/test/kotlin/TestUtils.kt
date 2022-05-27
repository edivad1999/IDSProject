import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import routes.auth.JavaBase64Encoder
import routes.auth.RegisterRequest
import routes.auth.Role
import java.net.URI

fun testWithApplicationContext(test: (Database) -> Unit) {
    val database = if (System.getenv("DATABASE_URL") != null) {
        val dbUri = URI(System.getenv("DATABASE_URL"))

        val username: String = dbUri.userInfo.split(":")[0]
        val password: String = dbUri.userInfo.split(":")[1]
        val dbUrl = "jdbc:postgresql://" + dbUri.host + ':' + dbUri.port + dbUri.path
        Database.connect(dbUrl, driver = "org.postgresql.Driver", user = username, password = password)


    } else {
        Database.connect("jdbc:postgresql://localhost:5439/postgres", driver = "org.postgresql.Driver",
            user = "postgres", password = "postgres")
    }
    test(database)

}

fun provideClient() = HttpClient(CIO) {
    install(JsonFeature) {
        serializer = KotlinxSerializer()
        acceptContentTypes = listOf(
            ContentType.parse("application/json")
        )
    }
}

val testEndPoint = "http://localhost:42069/api"

fun createMockUser(username: String, password: String, email: String, role: Role): Boolean {
    val encoder = JavaBase64Encoder()
    return runBlocking {
        provideClient().post<HttpStatusCode>("$testEndPoint/register") {
            parameter("role", role.name)
            contentType(ContentType.Application.Json)
            body = RegisterRequest(encoder.encodeString(username), encoder.encodeString(email), encoder.encodeString(password))
        }.isSuccess()
    }

}
