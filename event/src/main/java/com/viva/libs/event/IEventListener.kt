package com.viva.libs.event

interface IEventListener<E>{
    fun call(event: E)
    fun success(event:E)
    fun fail(event: E)
    fun error(error:E)
}