package example.com.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.*

fun Application.configureDatabases() {
    val dbConnection: java.sql.Connection = connectToPostgres(embedded = true)
    val chatService = ChatService(dbConnection)
    
    routing {

        get("/connect") {
            call.respond(dbConnection.isClosed)
        }
        post("/register"){
            val user = call.receive<Register>()
            val id = chatService.registerUser(user)
            call.respond(HttpStatusCode.Created, id)
        }

        get("/getallusers"){
            call.respond(chatService.getUsers())
        }

        delete("/delete"){
            chatService.dropTable()
        }
    
        // Read city
        get("/cities/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            try {
                val city = chatService.read(id)
                call.respond(HttpStatusCode.OK, city)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    
        // Update city
        put("/cities/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = call.receive<City>()
            chatService.update(id, user)
            call.respond(HttpStatusCode.OK)
        }
    
        // Delete city
        delete("/cities/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            chatService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}
/*
*
 * Makes a connection to a Postgres database.
 *
 * In order to connect to your running Postgres process,
 * please specify the following parameters in your configuration file:
 * - postgres.url -- Url of your running database process.
 * - postgres.user -- Username for database connection
 * - postgres.password -- Password for database connection
 *
 * If you don't have a database process running yet, you may need to [download]((https://www.postgresql.org/download/))
 * and install Postgres and follow the instructions [here](https://postgresapp.com/).
 * Then, you would be able to edit your url,  which is usually "jdbc:postgresql://host:port/database", as well as
 * user and password values.
 *
 *
 * @param embedded -- if [true] defaults to an embedded database for tests that runs locally in the same process.
 * In this case you don't have to provide any parameters in configuration file, and you don't have to run a process.
 *
 * @return [Connection] that represent connection to the database. Please, don't forget to close this connection when
 * your application shuts down by calling [Connection.close]
 * */

fun Application.connectToPostgres(embedded: Boolean): Connection {
    Class.forName("org.postgresql.Driver")
//    if (embedded) {
//        return DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "root", "")
//    } else {
        val url = "jdbc:postgresql://localhost:5432/postgres" // environment.config.property("postgres.url").getString()
        val user = "postgres" // environment.config.property("postgres.user").getString()
        val password = "newpass" // environment.config.property("postgres.password").getString()

        return DriverManager.getConnection(url, user, password)
//    }
}
