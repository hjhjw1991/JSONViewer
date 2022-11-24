package com.hjhjw1991.barney.jsonviewer

import com.hjhjw1991.barney.jsonviewer.storage.IRepo
import com.hjhjw1991.barney.jsonviewer.storage.IStorage
import java.lang.NullPointerException
import java.util.concurrent.ConcurrentHashMap

/**
 * @author huangjun.barney
 * @since 2022/11/24
 */

// only memory cache
object SimpleKV: IStorage {
    private val repoMap = mutableMapOf<String, IRepo>()
    override fun getRepo(repoKey: String): IRepo {
        return repoMap[repoKey] ?: SimpleRepo().apply {
            repoMap[repoKey] = this
        }
    }
}

// only memory cache
@Suppress("UNCHECKED_CAST")
class SimpleRepo: IRepo {
    private val storeMap: MutableMap<String, Any> = ConcurrentHashMap<String, Any>()
    override fun getString(key: String, default: String?): String? {
        return get(key, default)
    }

    override fun setString(key: String, value: String?) {
        set(key, value)
    }

    override fun getInt(key: String, default: Int): Int {
        return get(key, default) as Int
    }

    override fun setInt(key: String, value: Int) {
        set(key, value)
    }

    override fun getLong(key: String, default: Long): Long {
        return get(key, default) as Long
    }

    override fun setLong(key: String, value: Long) {
        set(key, value)
    }

    override fun getStringArray(key: String, default: List<String>): List<String> {
        return get(key, default) as List
    }

    override fun setStringArray(key: String, value: List<String>) {
        set(key, value)
    }

    override fun getStringSet(key: String, default: Set<String>): Set<String> {
        return get(key, default) as Set
    }

    override fun setStringSet(key: String, value: Set<String>) {
        set(key, value)
    }


    override fun <T> get(key: String, default: T?): T? {
        val obj = storeMap[key]
        return obj as? T ?: default
    }

    fun <T> set(key: String, value: T) {
        if (value == null) {
            HLog.loge(NullPointerException().stackTraceToString())
        } else {
            storeMap[key] = value as Any
        }
    }

    override fun contains(key: String): Boolean {
        return storeMap.containsKey(key)
    }

    override fun remove(key: String) {
        storeMap.remove(key)
    }

    override fun size(): Int {
        return storeMap.size
    }

    override fun clear() {
        storeMap.clear()
    }
}