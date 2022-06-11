package com.viva.libs

import com.viva.libs.event.EventDispatcher
import com.viva.libs.event.EventType
import com.viva.libs.event.Event
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun dispatch_once() {
        val d = EventDispatcher<EventType, Event<Int>>()
        d.once(EventType.ERROR){
            assertEquals(1,it.data)
        }
        d.dispatch(EventType.ERROR, Event(1))
        d.dispatch(EventType.ERROR, Event(2))
    }
}