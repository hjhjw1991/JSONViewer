package com.hjhjw1991.barney.jsonviewer.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hjhjw1991.barney.jsonviewer.HLog
import com.hjhjw1991.barney.jsonviewer.R
import com.hjhjw1991.barney.jsonviewer.SimpleKV
import com.hjhjw1991.barney.jsonviewer.storage.IRepo
import kotlinx.android.synthetic.main.activity_layout_jsonviewer.*

/**
 * json编辑器
 * 启动本activity时, 需传入extra, 其key为 DiamondDslTest.DIAMOND_DSL_STRING, 其value 表示在指定的keva中的json数据的key
 * keva为"diamond_dsl"
 * 我会尝试从keva中读取该字段, 并当做JSONObject来解析
 * 保存时, 也会写入到该字段
 * @author huangjun.barney
 * @since 2022/11/24
 */
class JSONViewerActivity : AppCompatActivity() {
    private lateinit var jsonViewLayout: JsonViewLayout
    companion object {
        const val EDIT_ROOT_HOLDER_KEY = "JSONObject"
        const val REPO_KEY = "json_viewer"
        const val JSON_STRING_KEY = "json_viewer_string_key"
    }
    private var json: String? = null
    private val repo: IRepo = SimpleKV.getRepo(REPO_KEY)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_jsonviewer)
        val jsonKey = intent.extras?.getString(JSON_STRING_KEY) ?: return
        jsonViewLayout = findViewById(R.id.json)
        repo.getString(jsonKey, null)?.let {
            json = it
            jsonViewLayout.bindJson(json)
        }
        save_btn.setOnClickListener {
            HLog.log("saving json to key $jsonKey: ")
            val json = jsonViewLayout.getJson()
            HLog.log(json.toString())
            repo.setString(jsonKey, json.getJSONObject(EDIT_ROOT_HOLDER_KEY).toString())
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
        }

        content.isFocusable = true
        content.isFocusableInTouchMode = true
        content.setOnTouchListener { v, event ->
            content.requestFocus()
            true
        }
    }
}