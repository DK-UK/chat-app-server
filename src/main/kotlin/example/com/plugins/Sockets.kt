package example.com.plugins

import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.reflect.*
import io.ktor.websocket.*
import io.ktor.websocket.serialization.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonEncoder
import java.time.Duration
import java.util.Collections
import java.util.LinkedHashSet
import java.util.concurrent.atomic.AtomicInteger

const val CHAT_PARTNER = "_CHAT_PARTNER_"
fun Application.configureSockets() {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        val allMemberSessions = Collections.synchronizedSet<TestConnection?>(LinkedHashSet())


        webSocket("/chat") {
            var chatMessage : ChatMessage = ChatMessage()
            println("Adding user!")
                val thisConnection = TestConnection(this)
                allMemberSessions += thisConnection


                try{
                    println("you are connected!, there are ${allMemberSessions.count()} users online.")
                    chatMessage.senderId = thisConnection.name
                    val users = allMemberSessions/*.filter { it.session != this }*/.map { it.name }

                    // send all users details to the client
                    // for choose to interaction
                    allMemberSessions.forEach { current->
                        current.session.outgoing.send(Frame.Text("NEW_USERS ${users.filter { it != current.name }.joinToString(",")}"))
                    }

                    for (frame in incoming){
                        frame as? Frame.Text ?: continue
                        val receivedText = frame.readText() as String
                        try{
                            chatMessage = Json.decodeFromString<ChatMessage>(receivedText)
                        }
                        catch (e : Exception){
                            println("JSON EXE : ${e.toString()}")
                        }
                        val user = if(receivedText.contains(CHAT_PARTNER)) receivedText.substringAfter(CHAT_PARTNER).trimStart() else ""
                        println("USER : $user")
                        if (user.isNotEmpty()){
                            chatMessage.receiverId = user
                            sendSerialized(chatMessage)
                        }

                        val senderSession = allMemberSessions.find { it.name == chatMessage.senderId }?.session
                        val receiverSession = allMemberSessions.find { it.name == chatMessage.receiverId }?.session

                        if (senderSession != null && receiverSession != null && chatMessage.message.isNotEmpty()){
                            val encodedMessage = Json.encodeToString<ChatMessage>(chatMessage)
//                            senderSession.send(Frame.Text(encodedMessage))
                            receiverSession.send(Frame.Text(encodedMessage))
                        }
                    }

                }
                catch (e : Exception){
                    println("error : ${e.localizedMessage}")
                }

            Frame.Close(CloseReason(CloseReason.Codes.NORMAL, "all done!!"))
        }

    }
}

@Serializable
data class ChatMessage(
    var senderId : String? = null,
    var receiverId : String? = null,
    var message : String = ""
)

class TestConnection(val session : DefaultWebSocketSession){
    companion object{
        val lastId = AtomicInteger(0)
    }
    var name = "user${lastId.getAndIncrement()}"
}