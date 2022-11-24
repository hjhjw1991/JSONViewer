package com.hjhjw1991.barney.jsonviewer.ui;

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hjhjw1991.barney.jsonviewer.R
import com.hjhjw1991.barney.jsonviewer.SimpleKV
import com.hjhjw1991.barney.jsonviewer.ui.JSONViewerActivity.Companion.REPO_KEY
import kotlinx.android.synthetic.main.activity_layout_launcher.*
import org.json.JSONObject

/**
 * @author huangjun.barney
 * @since 2022/11/25
 */
/*
TODO feature list
- show json editor in fragment
- import json from/export json to clipboard
- import json from/export json to external storage
 */
class LauncherActivity: AppCompatActivity() {
    private val repo = SimpleKV.getRepo(REPO_KEY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_launcher)
        buildDemoJson().let {
            val storeKey = "demo_key"
            repo.setString(storeKey, it.toString())
            startActivity(Intent(this, JSONViewerActivity::class.java).apply {
                putExtra(JSONViewerActivity.JSON_STRING_KEY, storeKey)
            })
        }
        splash.setOnClickListener {
            goNext()
        }
    }

    private fun goNext() {
        val storeKey = "demo_key"
        startActivity(Intent(this, JSONViewerActivity::class.java).apply {
            putExtra(JSONViewerActivity.JSON_STRING_KEY, storeKey)
        })
    }

    private fun buildDemoJson(): JSONObject {
        return JSONObject().apply {
            add(
                "Name" to "Alice",
                "Age" to 27,
                "Gender" to "Female",
                "Graduated" to true,
            )
        }
    }

    private fun JSONObject.add(vararg pair: Pair<String, Any>) {
        pair.forEach {
            this.put(it.first, it.second)
        }
    }
}
