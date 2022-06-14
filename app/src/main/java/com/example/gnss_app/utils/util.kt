package com.example.gnss_app.utils

import android.content.Context
import android.widget.Toast

fun toastShow(context: Context, tag: String = "TAG", msg: String) =
    Toast.makeText(context, "$tag $msg", Toast.LENGTH_SHORT).show()


fun ByteArray.readInt8(startFrom: Int = 0): UInt {
    return this[startFrom].toUInt()
}

fun ByteArray.readInt16(startFrom: Int = 0): UInt {
    return this[startFrom].toUInt().shl(8) or this[startFrom + 1].toUInt()
}

fun ByteArray.readUInt(startFrom: Int = 0): UInt {
    var rs = 0u
    for (i in startFrom..startFrom + 3) {
        rs = rs shl 8 or this[i].toUInt()
    }
    return rs
}
