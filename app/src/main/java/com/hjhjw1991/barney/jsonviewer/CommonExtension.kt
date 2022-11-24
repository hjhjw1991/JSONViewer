package com.hjhjw1991.barney.jsonviewer

import org.json.JSONArray
import org.json.JSONObject

/**
 * @author huangjun.barney
 * @since 2022/11/24
 */

fun Exception.getStackTraceString(): String = this.stackTraceToString()

fun <T> T?.orDefault(other: T): T {
    return this ?: other
}

fun JSONObject.merge(other: JSONObject?, override: Boolean = false): JSONObject {
    if (other != null) {
        for (key in other.keys()) {
            if (!this.has(key) || override) {
                this.put(key, other.get(key))
            }
        }
    }
    return this
}

inline fun <reified T> JSONArray.forEachIndexed(block: (index: Int, T) -> Unit) {
    for(i in 0 until length()) {
        (get(i) as? T)?.let {
            block(i, it)
        }
    }
}

inline fun <reified T> JSONArray.forEach(block: (T) -> Unit) {
    for(i in 0 until length()) {
        (get(i) as? T)?.let {
            block(it)
        }
    }
}

inline fun JSONObject.forEach(block: (Any) -> Unit) {
    keys().forEach {
        block(get(it))
    }
}

inline fun JSONObject.forEachEntry(block: (String, Any) -> Unit) {
    keys().forEach {
        block(it, get(it))
    }
}