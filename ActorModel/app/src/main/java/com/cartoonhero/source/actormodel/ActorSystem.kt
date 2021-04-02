package com.cartoonhero.source.actormodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

interface Message

@ExperimentalCoroutinesApi
class ActorSystem {
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