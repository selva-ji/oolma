package dev.selva // Or whatever your package name is

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.*
import kotlinx.coroutines.channels.consumeEach

// Data class to hold connection information
data class Connection(val session: DefaultWebSocketSession, val id: String)

fun main(args: Array<String>) {
    // This starts the server and tells it to run the 'module' function
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // This function now contains all our server logic.
    install(WebSockets)

    val connections = Collections.synchronizedSet<Connection>(LinkedHashSet())

    routing {
        webSocket("/ws") {
            val thisConnection = Connection(this, UUID.randomUUID().toString())
            connections += thisConnection
            println("New client connected: ${thisConnection.id}. Total clients: ${connections.size}")

            try {
                // If we now have two clients, tell the FIRST client to create an offer
                if (connections.size == 2) {
                    connections.firstOrNull()?.let { firstConnection ->
                        firstConnection.session.send("create_offer")
                        println("Told client ${firstConnection.id} to create an offer.")
                    }
                }

                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        // Find the other client and send the message to them
                        val otherConnection = connections.find { it != thisConnection }
                        otherConnection?.session?.send(text)
                    }
                }
            } catch (e: Exception) {
                println("Error with client ${thisConnection.id}: ${e.localizedMessage}")
            } finally {
                connections -= thisConnection
                println("Client ${thisConnection.id} disconnected. Total clients: ${connections.size}")
            }
        }
    }
}

