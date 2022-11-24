package com.hjhjw1991.barney.jsonviewer

import android.util.Log

/**
 * @author huangjun.barney
 * @since 2022/11/24
 */
object HLog {
    private val TAG = "HLog"
    fun log(vararg msg: String) {
        Log.d(TAG, msg.joinToString(separator = ","))
    }
    fun loge(vararg msg: String, t: Throwable? = null) {
        Log.e(TAG, msg.joinToString(separator = ","), t)
    }
}