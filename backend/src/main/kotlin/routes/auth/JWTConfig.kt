package routes.auth

import com.auth0.jwt.*
import com.auth0.jwt.algorithms.*
import kotlinx.serialization.Serializable
import model.dao.UserEntity
import java.util.*

object JwtConfig {

    private const val secret = "17ED96E5E68934C2910E371BA4B22B07B4BEDC52012DC5E0751BEF330F578D89"
    private const val issuer = "edivad99"
    private const val validityInMs = 3600000 // 1 hour
    private val algorithm = Algorithm.HMAC512(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    /**
     * Produce a token for this combination of User and Account
     */
    fun makeToken(user: UserEntity): AuthTokenResponseData {
        val exp = getExpiration()
        val jwt = JWT.create()
            .withSubject("Authentication")
            .withIssuer(issuer)
            .withClaim("username", user.username)
            .withClaim("role", user.role)
            .withExpiresAt(exp)
            .sign(algorithm)
        return AuthTokenResponseData(jwt, exp.toInstant().toEpochMilli())
    }


    /**
     * Calculate the expiration Date based on current time + the given validity
     */
    private fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)
}

@Serializable
data class AuthTokenResponseData(val jwt: String, val expAt: Long)
