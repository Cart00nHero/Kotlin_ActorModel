package com.cartoonhero.source.actormodel

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.*
import kotlin.coroutines.EmptyCoroutineContext

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
abstract class Actor {

    private interface Message
    private val mSystem = ActorSystem()
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private data class ActorMessage(
        val send: () -> Unit
    ): Message

    init {
        startScope()
    }

    private fun startScope() = scope.launch {
        val actor = actor<Message>(scope.coroutineContext) {
            for (msg in channel) {
                act(msg)
            }
        }
        mSystem.mailbox.collect(actor::send)
    }
    private fun sendMessage(message: Message) = mSystem.send(message)
    private fun act(message: Message) {
        when(message) {
            is ActorMessage -> message.send()
        }
    }

    fun start() {
        if (!scope.isActive) startScope()
    }
    fun send(portal: () -> Unit) {
        sendMessage(ActorMessage(portal))
    }
    fun cancel() {
        if (scope.isActive) scope.cancel()
    }

    @ExperimentalCoroutinesApi
    private inner class ActorSystem {
        private val scope: CoroutineScope =
            CoroutineScope(EmptyCoroutineContext + SupervisorJob())
        private val channel: Channel<Message> = Channel(100)
//    private val channel: BroadcastChannel<Message> = BroadcastChannel(100)

        fun send(event: Message) {
            scope.launch {
                channel.send(event)
            }
        }

        val mailbox: Flow<Message>
            get() = flow { emitAll(channel.receiveAsFlow()) }
//        get() = flow { emitAll(channel.openSubscription()) }
    }
}