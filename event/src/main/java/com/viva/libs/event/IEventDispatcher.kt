package com.viva.libs.event

interface IEventDispatcher<T, E> {

    fun on(e: T, fu: (E) -> Unit)
    fun on(e: T, listener: IEventListener<E>)
    fun off(e: T, fu: (E) -> Unit)
    fun off(e: T, listener: IEventListener<E>)
    fun dispatch(e: T, event: E)
    fun once(e: T, fu: (E) -> Unit)
    fun destroy()
}