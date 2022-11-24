package com.hjhjw1991.barney.jsonviewer.storage

/**
 * 内存缓存或持久化存储
 * @author huangjun.barney
 * @since 2022/11/24
 */
interface IStorage {
    // will create a repo if not exist
    fun getRepo(repoKey: String): IRepo
}
