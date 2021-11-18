import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.instance
import org.kodein.di.ktor.di
import java.util.*


inline fun <reified T : Any> Application.instance(tag: Any? = null) =
    di().instance<T>(tag)

inline fun <reified T : Any> Route.instance(tag: Any? = null) =
    di().instance<T>(tag)

inline fun <reified T : Any> PipelineContext<Unit, ApplicationCall>.instance(tag: Any? = null) =
    di().instance<T>(tag)


