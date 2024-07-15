package example.com.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        staticResources("/static", "static")
        route("/test2"){
            get {
                call.respondText("testing route default")
            }
            get("/next"){
                call.respondText("testing route next block")
            }
        }

        get("/test"){
            val testingData = mutableMapOf("a" to 1, "b" to 2, "c" to 3, "d" to 4, "e" to 5, "f" to 6)
            call.respond(testingData)
        }
    }
}
