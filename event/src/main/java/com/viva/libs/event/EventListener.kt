package com.viva.libs.event

open class EventListener<E>(val fu: (E) -> Unit): IEventListener<E> {
    override fun call(event: E) {
        fu(event)
    }

    override fun success(event: E) {
        TODO("Not yet implemented")
    }

    override fun fail(event: E) {
        TODO("Not yet implemented")
    }
    override fun error(error: E) {
        TODO("Not yet implemented")
    }
    override fun hashCode(): Int {
        return fu.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventListener<*>

        if (fu != other.fu) return false

        return true
    }

}