package di

import com.thedeanda.lorem.Lorem
import com.thedeanda.lorem.LoremIpsum
import di.serializers.DayOfWeekSerializer
import di.serializers.InstantSerializer
import di.serializers.LocalTimeSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.jetbrains.exposed.sql.Database
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import routes.auth.*
import java.io.File
import java.net.URI


object DIModules {

    val serialization
        get() = DI.Module("serialization") {
            bind<Json>() with singleton {
                Json(from = Json.Default) {
                    serializersModule = instance()
                    prettyPrint = true
                    prettyPrintIndent = "  "
                    encodeDefaults = true
                }
            }
            bind<SerializersModule>() with singleton {
                SerializersModule {
                    contextual(InstantSerializer)
                    contextual(LocalTimeSerializer)
                    contextual(DayOfWeekSerializer)
                }

            }
            bind<Lorem>() with singleton {
                LoremIpsum.getInstance()
            }

            bind<Base64Encoder>() with singleton { JavaBase64Encoder() }
        }
    val database
        get() = DI.Module("database") {
            bind<Database>() with singleton {
                if (System.getenv("DATABASE_URL") != null) {
                    val dbUri = URI(System.getenv("DATABASE_URL"))

                    val username: String = dbUri.userInfo.split(":")[0]
                    val password: String = dbUri.userInfo.split(":")[1]
                    val dbUrl = "jdbc:postgresql://" + dbUri.host + ':' + dbUri.port + dbUri.path
                    Database.connect(dbUrl, driver = "org.postgresql.Driver", user = username, password = password)


                } else {
                    Database.connect("jdbc:postgresql://localhost:5439/postgres", driver = "org.postgresql.Driver",
                        user = "postgres", password = "postgres")
                }

            }

            bind<File>() with singleton {
                kotlin.io.path.createTempFile("tmpLog", ".log").toFile().also {
                    println("LOGDIR -> ${it.absolutePath}")
                }
            }
        }

    val security
        get() = DI.Module("security") {
            bind<JWTCredentialsVerifier>() with singleton {
                MyJWTCredentialVerifier(di)
            }

            bind<PasswordDigester>() with singleton { SHA256Digester() }

        }

}
