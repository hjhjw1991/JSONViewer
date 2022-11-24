package com.hjhjw1991.barney.jsonviewer.storage

/**
 * @author huangjun.barney
 * @since 2022/11/24
 */
interface IRepo {
    fun getString(key: String, default: String?): String?
    fun setString(key: String, value: String?)
    fun getInt(key: String, default: Int): Int
    fun setInt(key: String, value: Int)
    fun getLong(key: String, default: Long): Long
    fun setLong(key: String, value: Long)
    fun getStringArray(key: String, default: List<String>): List<String>
    fun setStringArray(key: String, value: List<String>)
    fun getStringSet(key: String, default: Set<String>): Set<String>
    fun setStringSet(key: String, value: Set<String>)
    fun <T> get(key: String, default: T?): T?
    fun contains(key: String): Boolean
    fun remove(key: String)
    fun size(): Int
    fun clear()
}