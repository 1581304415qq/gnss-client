package com.viva.libs

import com.viva.libs.utils.CircleByteBuffer
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CircleByteBufferTest {

    private lateinit var circleByteBuffer: CircleByteBuffer

    @Before
    fun setup() {
        circleByteBuffer = CircleByteBuffer(128)
    }

    @Test
    fun `put 127 gets 128`() {
        circleByteBuffer.puts(ByteArray(127))
        val exception = Assert.assertThrows(Exception::class.java) { circleByteBuffer.get(128) }
        Assert.assertEquals("out buffer", exception.message)
    }

    @Test
    fun `put 127 getlen 127`() {
        circleByteBuffer.puts(ByteArray(127) { 1 })
        Assert.assertEquals(127, circleByteBuffer.getLen())
    }
    @Test
    fun `put 127 gets 127`() {
        circleByteBuffer.puts(ByteArray(127) { 1 })
        Assert.assertEquals(127, circleByteBuffer.gets(circleByteBuffer.getLen()).size)
    }
    @Test
    fun `put 128 getlen 0`() {
        circleByteBuffer.puts(ByteArray(128){1})
        Assert.assertEquals(0,circleByteBuffer.getLen())
    }

    @Test
    fun `put 128 get 129`() {
        circleByteBuffer.puts(ByteArray(128))
        val exception = Assert.assertThrows(Exception::class.java) { circleByteBuffer.get(129) }
        Assert.assertEquals("out buffer", exception.message)
    }

    @Test
    fun `put 128 getLen 128`() {
        val exception = Assert.assertThrows(Exception::class.java) { circleByteBuffer.get(129) }
        Assert.assertEquals("out buffer", exception.message)
    }
}