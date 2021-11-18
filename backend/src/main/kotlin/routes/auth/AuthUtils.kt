package routes.auth


import io.ktor.auth.*
import io.ktor.auth.jwt.*
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import model.dao.UserAuth
import model.tables.UserAuthTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*


fun generateRandomAlphanumericalString(STRING_LENGTH: Int): String {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..STRING_LENGTH)
        .map { kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
}

interface PasswordDigester {

    fun digest(password: String): String

    operator fun invoke(password: String) =
        digest(password)

}

class SHA256Digester : PasswordDigester {
    override fun digest(password: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())
            .fold("") { str, byte ->
                str + "%02x".format(byte)
            }
}

interface JWTCredentialsVerifier {
    fun verify(jwtCredential: JWTCredential): BasePrincipal?

    operator fun invoke(jwtCredential: JWTCredential) =
        verify(jwtCredential)
}

class MyJWTCredentialVerifier(override val di: DI) : JWTCredentialsVerifier, DIAware {

    override fun verify(jwtCredential: JWTCredential): BasePrincipal? {
        val username = jwtCredential.payload.getClaim("username").asString()
        val role = Role.valueOf(jwtCredential.payload.getClaim("role").asString())
        val expiresAt = jwtCredential.payload.expiresAt.toInstant()
        if (expiresAt.isBefore(Clock.System.now().toJavaInstant())) return null
        val db: Database by instance()
        val user = transaction(db) {
            UserAuth.find {
                UserAuthTable.username eq username
            }.firstOrNull()
        }
        return if (user == null) null else BasePrincipal(user.username, role)
    }

}

enum class Role {
    USER, ADMIN
}

data class BasePrincipal(val userId: String, val role: Role) : Principal


interface Base64Encoder {

    fun encode(byteArray: ByteArray): String
    fun decode(string: String): ByteArray

    fun decodeString(string: String, charset: Charset = Charsets.UTF_8) =
        decode(string).toString(charset)

    fun encodeString(string: String) =
        encode(string.toByteArray())

}

class JavaBase64Encoder : Base64Encoder {

    private val encoder by lazy { Base64.getEncoder()!! }
    private val decoder by lazy { Base64.getDecoder()!! }

    override fun encode(byteArray: ByteArray) =
        encoder.encodeToString(byteArray)!!

    override fun decode(string: String) =
        decoder.decode(string)!!

}
