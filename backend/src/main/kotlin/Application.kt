import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main(args: Array<String>): Unit {
    embeddedServer(CIO, port = (System.getenv("PORT")?:"42069").toInt()) {
        managerModule()
    }.start(wait = true)
}

