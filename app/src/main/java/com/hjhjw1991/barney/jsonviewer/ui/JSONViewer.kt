package com.hjhjw1991.barney.jsonviewer.ui

import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.hjhjw1991.barney.jsonviewer.*
import com.hjhjw1991.barney.jsonviewer.ui.JSONViewerActivity.Companion.EDIT_ROOT_HOLDER_KEY
import kotlinx.android.synthetic.main.jsonviewer_json_edit_dialog.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Created by huangjun.barney
 */
class JsonViewLayout : ScrollView {
    private var ITEM_KEY_COLOR = -0x9e58cf
    private var OBJECT_KEY_COLOR = -0xcccccd
    private var TEXT_COLOR = -0x1190bd
    private var NUMBER_COLOR = -0x3cac0a
    private var ARRAY_LENGTH_COLOR = -0x1
    private var BOOLEAN_COLOR = -0xbf6739
    private val NULL_COLOR = -0x4345a8
    private val TEXT_SIZE_DP_MAX = 32
    private val TEXT_SIZE_DP_MIN = 12
    private val ARRAY_LENGTH_BACKGROUND: Int = R.drawable.jsonview_select_bg
    private var mContext: Context? = null
    private var mJSONObject: JSONObject? = null
    private var mJSONArray: JSONArray? = null
    private var contentView: LinearLayout? = null
    private var horizontalScrollView: HorizontalScrollView? = null

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initView(context)
    }

    private fun initView(context: Context) {
        mContext = context
        contentView = LinearLayout(mContext)
        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        contentView?.layoutParams = layoutParams
        contentView?.orientation = LinearLayout.VERTICAL
        horizontalScrollView = HorizontalScrollView(mContext)
        horizontalScrollView?.layoutParams = layoutParams
        horizontalScrollView?.setPadding(12, 12, 12, 0)
        horizontalScrollView?.addView(contentView)
        this.addView(horizontalScrollView)
    }

    fun bindJson(jsonStr: String?) {
        require(!canBindData()) { "JsonViewLayout can not bind again." }
        var jsonObject: Any? = null
        try {
            jsonObject = JSONTokener(jsonStr).nextValue()
        } catch (e: JSONException) {
            HLog.loge(e.getStackTraceString())
        }
        if (jsonObject != null && jsonObject is JSONObject) {
            mJSONObject = jsonObject
        } else if (jsonObject != null && jsonObject is JSONArray) {
            mJSONArray = jsonObject
        } else {
            throw IllegalArgumentException("jsonStr is illegal.")
        }
        createView()
    }

    fun getJson(): JSONObject {
        val view = contentView!!.getChildAt(0) as JsonItemView
        val root = view.getData()
        return root?.convertToJSONObject().orDefault(JSONObject())
    }

    fun bindJson(mJSONObject: JSONObject?) {
        require(!canBindData()) { "JsonViewLayout can not bind again." }
        this.mJSONObject = mJSONObject
        requireNotNull(mJSONObject) { "jsonObject can not be null." }
        createView()
    }

    private fun canBindData(): Boolean {
        return null != mJSONObject || null != mJSONArray
    }

    private fun createView() {
        val context = mContext ?: return
        val jsonView = JsonItemView(context)
        jsonView.showIcon(true)
        jsonView.hideValue()
        val root = if (mJSONObject != null) {
            JsonObjectItem(EDIT_ROOT_HOLDER_KEY, mJSONObject!!)
        } else {
            JsonArrayItem(EDIT_ROOT_HOLDER_KEY, mJSONArray!!)
        }
        jsonView.setData(root)
        jsonView.showKey(root.name)
        jsonView.setIconClickListener(JsonViewExpandCollapseListener(jsonView, 0))
        contentView?.addView(jsonView)
    }

    private fun handleJsonObject(itemView: JsonItemView, hierarchy: Int) {
        val data = itemView.getData() ?: return
        val key = data.name
        val value = data.value
        itemView.hideIcon()
        if (value is JSONObject) {
            itemView.showIcon(true)
            itemView.setIconClickListener(JsonViewExpandCollapseListener(itemView, hierarchy + 1))
            itemView.setCommand(getHierarchyStr(hierarchy + 1))
        } else {
            if (value is JSONArray) {
                itemView.showIcon(true)
                itemView.setIconClickListener(
                    JsonViewExpandCollapseListener(
                        itemView,
                        hierarchy + 1
                    )
                )
                itemView.setCommand(getHierarchyStr(hierarchy + 1))
                itemView.showValue("  " + value.length() + "  ")
                itemView.showArrayLength(ARRAY_LENGTH_BACKGROUND)
            } else {
                itemView.hideIcon()
                itemView.showValue(value.toString())
                itemView.setCommand(getHierarchyStr(hierarchy + 1))
                itemView.showEdit(true)
            }
        }
        itemView.showKey(key)
    }

    internal inner class JsonViewExpandCollapseListener(
        itemView: JsonItemView,
        hierarchy: Int
    ):
        OnClickListener {
        private val itemView: JsonItemView
        private val hierarchy: Int
        private var isexpand = true
        private var isJsonArray: Boolean
        override fun onClick(v: View) {
            val obj = v.tag
            val data = itemView.getData() ?: return
            var value = data.value
            if (obj != null && obj is Boolean) {
                isexpand = obj
                v.tag = null
            }
            if (itemView.childCount == 1) {
                var array =
                    if (isJsonArray) value as JSONArray? else (value as? JSONObject)?.names()
                if (null != array) {
                    isexpand = false
                    if (!isJsonArray && array.length() == 1 && "nameValuePairs" == array.opt(0)
                            .toString()
                    ) {
                        val nameValuePairs = (value as JSONObject?)?.opt("nameValuePairs")
                        if (null != nameValuePairs) {
                            value = nameValuePairs
                            isJsonArray = value is JSONArray
                            array =
                                if (isJsonArray) value as JSONArray else (value as JSONObject).names()
                        }
                    }
                    var i = 0
                    while (array != null && i < array.length()) {
                        val childItemView = JsonItemView(itemView.context)
                        val childValue = array.opt(i)
                        if (isJsonArray) {
                            childItemView.setData((data as JsonArrayItem).children[i])
                            handleJsonObject(childItemView, hierarchy)
                        } else {
                            childItemView.setData((data as JsonObjectItem).children[childValue]!!)
                            handleJsonObject(childItemView, hierarchy)
                        }
                        itemView.addViewNoInvalidate(childItemView)
                        i++
                    }
                } else {
                    isexpand = !isexpand
                }
                itemView.showIcon(isexpand)
                itemView.requestLayout()
                itemView.invalidate()
            } else {
                isexpand = !isexpand
                itemView.showIcon(isexpand)
                for (i in 1 until itemView.childCount) {
                    itemView.getChildAt(i).visibility = if (!isexpand) VISIBLE else GONE
                }
            }
        }

        init {
            this.itemView = itemView
            this.hierarchy = hierarchy
            isJsonArray = itemView.getData() is JsonArrayItem
        }
    }

    fun setTextSize(sizeDP: Float) {
        if (TEXT_SIZE_DP != sizeDP
                .coerceAtLeast(TEXT_SIZE_DP_MIN.toFloat())
                .coerceAtMost(TEXT_SIZE_DP_MAX.toFloat())
        ) {
            TEXT_SIZE_DP = sizeDP
            updateAll(sizeDP)
        }
    }

    fun updateAll(textSize: Float) {
        val count = this.childCount
        for (i in 0 until count) {
            contentView?.getChildAt(i)?.let {
                loop(it, textSize)
            }
        }
    }

    private fun loop(view: View, textSize: Float) {
        if (view is JsonItemView) {
            val group: JsonItemView = view
            group.setTextSize(textSize)
            val childCount: Int = group.childCount
            for (i in 0 until childCount) {
                val view1: View = group.getChildAt(i)
                loop(view1, textSize)
            }
        }
    }

    var mode = 0
    var oldDist = 0f
    private fun zoom(f: Float) {
        setTextSize(TEXT_SIZE_DP * (f / 100 + 1))
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        var intercept = false
        when (event.action and event.actionMasked) {
            MotionEvent.ACTION_DOWN -> mode = 1
            MotionEvent.ACTION_UP -> mode = 0
            MotionEvent.ACTION_POINTER_UP -> {
                intercept = true
                mode -= 1
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                intercept = false
                oldDist = spacing(event)
                mode += 1
            }
            MotionEvent.ACTION_MOVE -> if (mode >= 2) {
                val newDist = spacing(event)
                if (abs(newDist - oldDist) > 3f) {
                    zoom(newDist - oldDist)
                    oldDist = newDist
                }
            }
        }
        return if (intercept) intercept else super.dispatchTouchEvent(event)
    }

    fun setKeyColor(color: Int) {
        ITEM_KEY_COLOR = color
    }

    fun setObjectKeyColor(color: Int) {
        OBJECT_KEY_COLOR = color
    }

    fun setValueTextColor(color: Int) {
        TEXT_COLOR = color
    }

    fun setValueNumberColor(color: Int) {
        NUMBER_COLOR = color
    }

    fun setValueBooleanColor(color: Int) {
        BOOLEAN_COLOR = color
    }

    fun setValueNullColor(color: Int) {
        NUMBER_COLOR = color
    }

    fun setArrayLengthColor(color: Int) {
        ARRAY_LENGTH_COLOR = color
    }

    private fun getHierarchyStr(hierarchy: Int): String {
        val levelStr = StringBuilder()
        for (levelI in 0 until hierarchy) {
            levelStr.append("      ")
        }
        return levelStr.toString()
    }

    fun expandAll() {
        contentView?.let {
            clickAllView(it, false)
        }
    }

    fun collapseAll() {
        contentView?.let {
            clickAllView(it, true)
        }
    }

    private fun clickAllView(viewGroup: ViewGroup, collapse: Boolean) {
        if (viewGroup is JsonItemView) {
            val jsonView: JsonItemView = viewGroup
            operationJsonView(jsonView, collapse)
        }
        for (i in 0 until viewGroup.childCount) {
            val view = viewGroup.getChildAt(i)
            if (viewGroup is JsonItemView) {
                val jsonView: JsonItemView = viewGroup
                operationJsonView(jsonView, collapse)
            }
            if (view is ViewGroup) {
                clickAllView(view, collapse)
            }
        }
    }

    private fun operationJsonView(jsonView: JsonItemView?, collapse: Boolean) {
        if (jsonView != null) {
            if (collapse) {
                jsonView.expand()
            } else {
                jsonView.collapse()
            }
        }
    }

    companion object {
        var TEXT_SIZE_DP = 18f
    }
}

open class JsonItem<T>(var name: String, var value: T) {
    var parent: JsonItemContainer? = null
    override fun toString(): String {
        return "[$name:$value]"
    }
    open fun convertToJSONObject(): JSONObject = JSONObject().apply {
        put(name, value)
    }
}

interface JsonItemContainer {
    fun addChild(child: JsonItem<*>)
    fun removeChild(child: JsonItem<*>): JsonItem<*>?
    fun replaceChild(old: JsonItem<*>, new: JsonItem<*>): Boolean
}

class JsonObjectItem(name: String, value: JSONObject): JsonItem<JSONObject>(name, value), JsonItemContainer {
    val children = mutableMapOf<String, JsonItem<*>>()
    init {
        value.forEach { key, item ->
            val child = when(item) {
                is JSONObject -> JsonObjectItem(key, item)
                is JSONArray -> JsonArrayItem(key, item)
                is Boolean -> JsonBooleanItem(key, item)
                is String -> JsonStringItem(key, item)
                is Number -> JsonNumberItem(key, item)
                else -> JsonStringItem(key, item.toString())
            }
            addChild(child)
        }
    }

    override fun convertToJSONObject(): JSONObject {
        val res = JSONObject()
        val child = JSONObject()
        children.values.forEach {
            child.merge(it.convertToJSONObject())
        }
        res.put(name, child)
        return res
    }

    fun JSONObject.forEach(block: (String, Any) -> Unit) {
        this.keys().forEach {
            block(it, this.get(it))
        }
    }

    override fun addChild(child: JsonItem<*>) {
        children[child.name] = child
        child.parent = this
    }

    override fun removeChild(child: JsonItem<*>): JsonItem<*>? {
        return children.remove(child.name).apply {
            child.parent = null
        }
    }

    override fun replaceChild(old: JsonItem<*>, new: JsonItem<*>): Boolean {
        removeChild(old)
        addChild(new)
        return true
    }
}

class JsonArrayItem(name: String, value: JSONArray): JsonItem<JSONArray>(name, value), JsonItemContainer {
    val children = mutableListOf<JsonItem<*>>()
    init {
        value.forEachIndexed<Any> { idx, item ->
            val child = when(item) {
                is JSONObject -> JsonObjectItem("$idx", item)
                is JSONArray -> JsonArrayItem("$idx", item)
                is Boolean -> JsonBooleanItem("$idx", item)
                is String -> JsonStringItem("$idx", item)
                is Number -> JsonNumberItem("$idx", item)
                else -> JsonStringItem("$idx", item.toString())
            }
            addChild(child)
        }
    }

    override fun convertToJSONObject(): JSONObject {
        val res = JSONObject()
        val child = JSONArray()
        children.forEach {
            if (it is JsonPrimitiveItem) {
                child.put(it.value.toString())
            } else {
                child.put(it.convertToJSONObject())
            }
        }
        res.put(name, child)
        return res
    }

    override fun addChild(child: JsonItem<*>) {
        children.add(child)
        child.parent = this
    }

    override fun removeChild(child: JsonItem<*>): JsonItem<*>? {
        return if (children.remove(child)) child.apply {
            child.parent = null
        } else null
    }

    override fun replaceChild(old: JsonItem<*>, new: JsonItem<*>): Boolean {
        val index = children.indexOf(old)
        children.add(index, new)
        children.remove(old)
        old.parent = null
        new.parent = this
        return true
    }
}
open class JsonPrimitiveItem<T>(name: String, value: T): JsonItem<T>(name, value)
class JsonBooleanItem(name: String, value: Boolean): JsonPrimitiveItem<Boolean>(name, value)
class JsonNumberItem(name: String, value: Number): JsonPrimitiveItem<Number>(name, value)
class JsonStringItem(name: String, value: String): JsonPrimitiveItem<String>(name, value)

/**
 * 每一条JSON的key-value展示
 * JSON里是孩子的, 在view里也是孩子
 */
class JsonItemView : LinearLayout {

    companion object {
        var TEXT_SIZE_DP = 18f
        private var ITEM_KEY_COLOR = -0x9e58cf
        private var OBJECT_KEY_COLOR = -0xcccccd
        private var TEXT_COLOR = -0x1190bd
        private var NUMBER_COLOR = -0x3cac0a
        private var ARRAY_LENGTH_COLOR = -0x1
        private var BOOLEAN_COLOR = -0xbf6739
        private val NULL_COLOR = -0x4345a8
    }

    inner class JsonItemEditDialogBuilder(context: Context): AlertDialog.Builder(context) {
        private var mJsonItem: JsonItem<*>? = null
        private val mView by lazy {
            LayoutInflater.from(context).inflate(R.layout.jsonviewer_json_edit_dialog, null)
        }

        fun confirmDelete(jsonItem: JsonItem<*>?, deleteResult: (JsonItem<*>) -> Unit) {
            TODO("feature delete json")
        }

        fun showEdit(jsonItem: JsonItem<*>?, editResult: (JsonItem<*>) -> Unit) {
            mJsonItem = jsonItem ?: return
            when(jsonItem) {
                is JsonObjectItem -> {
                    initJsonObjectEdit(jsonItem)
                }
                is JsonArrayItem -> {
                    initJsonArrayEdit(jsonItem)
                }
                is JsonStringItem -> {
                    initJsonStringEdit(jsonItem)
                }
                is JsonNumberItem -> {
                    initJsonNumberEdit(jsonItem)
                }
                is JsonBooleanItem -> {
                    initJsonBooleanEdit(jsonItem)
                }
            }
            setPositiveButton("保存") { _, _ ->
                // check value valid
                val key = mView.edit_key.text.toString()
                val value = mView.edit_value.text.toString()
                var canSave = true
                val newItem = when(mView.type_group.checkedRadioButtonId) {
                    R.id.type_string -> {
                        // 字符串无需处理
                        if (jsonItem !is JsonStringItem) {
                            JsonStringItem(key, value).apply {
                                jsonItem.parent?.replaceChild(jsonItem, this)
                            }
                        } else {
                            jsonItem.apply {
                                this.name = key
                                this.value = value
                            }
                        }
                    }
                    R.id.type_boolean -> {
                        if (value != "true" && value != "false") {
                            Toast.makeText(context, "格式错误", Toast.LENGTH_SHORT).show()
                            canSave = false
                            jsonItem
                        } else {
                            if (jsonItem !is JsonBooleanItem) {
                                JsonBooleanItem(key, value.toBoolean()).apply {
                                    jsonItem.parent?.replaceChild(jsonItem, this)
                                }
                            } else {
                                jsonItem.apply {
                                    this.name = key
                                    this.value = value.toBoolean()
                                }
                            }
                        }
                    }
                    R.id.type_number -> {
                        val float = value.toFloatOrNull()
                        if (float == null) {
                            Toast.makeText(context, "格式错误", Toast.LENGTH_SHORT).show()
                            canSave = false
                            jsonItem
                        } else {
                            if (jsonItem !is JsonNumberItem) {
                                JsonNumberItem(key, float).apply {
                                    jsonItem.parent?.replaceChild(jsonItem, this)
                                }
                            } else {
                                jsonItem.apply {
                                    this.name = key
                                    this.value = float
                                }
                            }
                        }
                    }
                    else -> jsonItem
                }
                // call result
                if (canSave) {
                    editResult.invoke(newItem)
                }
            }
            setView(mView)
            show()
        }

        private fun initJsonBooleanEdit(jsonItem: JsonBooleanItem) {
            mView.edit_key.setText(jsonItem.name)
            mView.type_boolean.isChecked = true
            mView.edit_value.setText(jsonItem.value.toString())
        }

        private fun initJsonNumberEdit(jsonItem: JsonNumberItem) {
            mView.edit_key.setText(jsonItem.name)
            mView.type_number.isChecked = true
            mView.edit_value.setText(jsonItem.value.toString())
        }

        private fun initJsonStringEdit(jsonItem: JsonStringItem) {
            mView.edit_key.setText(jsonItem.name)
            mView.type_string.isChecked = true
            mView.edit_value.setText(jsonItem.value)
        }

        private fun initJsonArrayEdit(jsonItem: JsonArrayItem) {
            TODO("Not yet implemented")
        }

        private fun initJsonObjectEdit(jsonItem: JsonObjectItem) {
            TODO("Not yet implemented")
        }
    }

    private var imageview: ImageView? = null
    private var keyTv: TextView? = null
    private var valueTv: TextView? = null
    private var typeTv: TextView? = null
    private var commandTv: TextView? = null
    private var editBtn: ImageView? = null
    private var mData: JsonItem<*>? = null
    private var contentView: View? = null
    private var mContext: Context
    private var dataInvalid = 0x0
    private val VALUE_INVALID = 0x1
    private val KEY_INVALID = 0x2

    constructor(context: Context) : super(context) {
        mContext = context
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
        initView()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        mContext = context
        initView()
    }

    fun setData(item: JsonItem<*>) {
        mData = item
    }

    fun getData() = mData

    private fun initView() {
        orientation = VERTICAL
        LayoutInflater.from(mContext)
            .inflate(R.layout.jsonviewer_single_json_kv, this, true)
        imageview = findViewById(R.id.icon)
        keyTv = findViewById(R.id.key)
        valueTv = findViewById(R.id.value)
        typeTv = findViewById(R.id.type)
        commandTv = findViewById(R.id.command)
        editBtn = findViewById(R.id.edit)
        contentView = findViewById(R.id.content)
        contentView?.setBackgroundColor(Color.TRANSPARENT)
        imageview?.visibility = GONE
        keyTv?.visibility = GONE
        valueTv?.visibility = GONE
        typeTv?.visibility = GONE
        editBtn?.visibility = GONE
        setTextSize(TEXT_SIZE_DP)

        // 短按, 弹出编辑弹窗
        editBtn?.setOnClickListener {
            JsonItemEditDialogBuilder(context).showEdit(mData) {
                mData = it
                showKey(it.name)
                if (it is JsonPrimitiveItem) {
                    showValue(it.value.toString())
                }
                requestRedraw()
            }
        }
        // 长按删除, 弹出确认框
        editBtn?.setOnLongClickListener {
            JsonItemEditDialogBuilder(context).confirmDelete(mData) { deleted ->
                HLog.log(deleted.toString())
            }
            true
        }
    }

    fun requestRedraw() {
        invalidate()
    }

    fun setTextSize(textSizeDp: Float) {
        TEXT_SIZE_DP =
            textSizeDp.toInt().toFloat()
        keyTv?.textSize = TEXT_SIZE_DP
        valueTv?.textSize = TEXT_SIZE_DP
        commandTv?.textSize = TEXT_SIZE_DP
        val textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            TEXT_SIZE_DP,
            resources.displayMetrics
        ).toInt()
        val layoutParams = imageview?.layoutParams as LayoutParams
        layoutParams.height = textSize
        layoutParams.width = textSize
        val rightMargin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            JsonViewLayout.TEXT_SIZE_DP / 4,
            resources.displayMetrics
        ).toInt()
        layoutParams.rightMargin = rightMargin
        layoutParams.gravity = Gravity.CENTER_VERTICAL
        imageview?.layoutParams = layoutParams
    }

    fun setCommand(sequence: CharSequence?) {
        commandTv?.text = sequence
    }

    fun hideIcon() {
        imageview?.visibility = GONE
    }

    fun showIcon(canExpand: Boolean) {
        imageview?.visibility = VISIBLE
        imageview?.setImageResource(if (canExpand) R.drawable.jsonview_item_expand else R.drawable.jsonview_item_collapse)
    }

    fun hideValue() {
        valueTv?.visibility = GONE
    }

    fun showValue(s: CharSequence?) {
        val valueBuilder = SpannableStringBuilder()
        valueBuilder.append(s)
        val valueColor: Int = when (mData) {
            is JsonStringItem -> TEXT_COLOR
            is JsonBooleanItem -> BOOLEAN_COLOR
            is JsonNumberItem -> NUMBER_COLOR
            is JsonArrayItem -> ARRAY_LENGTH_COLOR
            else -> NULL_COLOR
        }
        valueBuilder.setSpan(
            ForegroundColorSpan(valueColor),
            0,
            valueBuilder.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        valueTv?.visibility = VISIBLE
        valueTv?.setText(valueBuilder, TextView.BufferType.SPANNABLE)
        valueTv?.setBackgroundColor(Color.TRANSPARENT)
    }

    fun showEdit(show: Boolean) {
        editBtn?.visibility = VISIBLE
    }

    fun showArrayLength(resId: Int) {
        valueTv?.setBackgroundResource(resId)
    }

    fun showKey(s: CharSequence?) {
        val keyBuilder = SpannableStringBuilder()
        val keyColor = if (mData is JsonPrimitiveItem) {
            keyBuilder.append("\"").append(s).append("\"").append(":")
            ITEM_KEY_COLOR
        } else {
            keyBuilder.append(s)
            OBJECT_KEY_COLOR
        }
        keyBuilder.setSpan(
            ForegroundColorSpan(keyColor),
            0,
            keyBuilder.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        keyTv?.visibility = VISIBLE
        keyTv?.setText(keyBuilder, TextView.BufferType.SPANNABLE)
    }

    fun setIconClickListener(listener: OnClickListener?) {
        imageview?.setOnClickListener(listener)
    }

    fun expand() {
        imageview?.tag = false
        imageview?.callOnClick()
    }

    fun collapse() {
        imageview?.tag = true
        imageview?.callOnClick()
    }

    fun addViewNoInvalidate(child: View) {
        var params = child.layoutParams
        if (params == null) {
            params = generateDefaultLayoutParams()
            requireNotNull(params) { "generateDefaultLayoutParams() cannot return null" }
        }
        addViewInLayout(child, -1, params)
    }
}