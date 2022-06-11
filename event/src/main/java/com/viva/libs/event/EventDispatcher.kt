package com.viva.libs.event

import java.util.*

/**
 * T    EventType enum
 * E    Event
 */
open class EventDispatcher<T, E> : IEventDispatcher<T, E> {
    private val binds: MutableMap<T, HashMap<Int, IEventListener<E>>> = mutableMapOf()
    override fun on(e: T, fu: (E) -> Unit) {
        on(e, EventListener(fu))
    }

    override fun on(e: T, listener: IEventListener<E>) {
        if (binds[e] == null) binds[e] = hashMapOf()
        binds[e]?.set(listener.hashCode(), listener)
    }

    override fun once(e: T, fu: (E) -> Unit) {
        on(e, object : EventListener<E>(fu) {
            override fun call(event: E) {
                off(e, fu)
                fu(event)
            }
        })
    }

    private val _lis: MutableMap<T, Int> = mutableMapOf()
    override fun off(e: T, fu: (E) -> Unit) {
        _lis[e] = fu.hashCode()
    }

    override fun off(e: T, listener: IEventListener<E>) {
        _lis[e] = listener.hashCode()
    }

    private fun removeListener() {
        _lis.forEach { (k, v) ->
            binds[k]?.remove(v)
        }
        _lis.clear()
    }

    override fun dispatch(e: T, event: E) {
        removeListener()
        binds[e]?.forEach { (_, listener) ->
            listener.call(event)
        }
        removeListener()
    }

    override fun destroy() {}
}