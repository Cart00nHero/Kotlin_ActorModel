package com.cartoonhero.source.actormodel

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.collect

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
abstract class Actor {

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
}