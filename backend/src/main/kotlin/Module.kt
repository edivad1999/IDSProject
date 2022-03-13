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
import model.tables.UsersTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.ktor.DIFeature
import org.kodein.di.ktor.di
import routes.auth.*


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
            clientiApi()

        }
    }

}

fun Application.initDb() = launch {
    val db: Database by instance()
    val digester: PasswordDigester by instance()
    transaction(db) {
        SchemaUtils.createMissingTablesAndColumns(UsersTable)

        val admin = UserEntity.find {
            UsersTable.username eq "admin"
        }
        if (admin.empty()) {
            UserEntity.new {
                username = "admin"
                hashPass = digester.digest("password")
            }
        }
    }
}

fun Authentication.Configuration.jwt(
    role: Role,
    configure: JWTAuthenticationProvider.Configuration.() -> Unit,
) = jwt(role.name, configure)
