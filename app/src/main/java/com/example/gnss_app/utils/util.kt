package com.example.gnss_app.utils

import android.content.Context
import android.widget.Toast

fun toastShow(context: Context, tag: String = "TAG", msg: String) =
    Toast.makeText(context, "$tag $msg", Toast.LENGTH_SHORT).show()