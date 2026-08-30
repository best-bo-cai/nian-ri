package com.nianri.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * 从 OpenAI 兼容（GET /v1/models）或 Anthropic 兼容（GET /v1/models）接口拉取模型列表。
 */
object ModelListFetcher {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun fetchModels(
        baseUrl: String,
        provider: String,
        apiKey: String
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(buildModelsUrl(baseUrl))
                .get()
                .apply {
                    when {
                        provider == "anthropic" -> {
                            if (apiKey.isNotEmpty()) addHeader("x-api-key", apiKey)
                            addHeader("anthropic-version", "2023-06-01")
                        }
                        apiKey.isNotEmpty() -> addHeader("Authorization", "Bearer $apiKey")
                    }
                }
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    throw Exception("HTTP ${response.code}：${body.take(100)}")
                }
                val models = parseModels(body)
                if (models.isEmpty()) {
                    throw Exception("接口未返回任何模型")
                }
                models
            }
        }
    }

    /**
     * 拼接模型列表接口 URL，兼容用户填写的各种 Base URL 形式：
     * - https://host
     * - https://host/v1
     * - https://host/v1/chat/completions（误填完整对话端点时自动修正）
     * - https://host/v1/messages
     */
    private fun buildModelsUrl(baseUrl: String): String {
        var t = baseUrl.trim().trimEnd('/')
        if (t.endsWith("/v1/models")) return t
        if (t.endsWith("/chat/completions")) t = t.removeSuffix("/chat/completions")
        if (t.endsWith("/messages")) t = t.removeSuffix("/messages")
        return if (t.endsWith("/v1")) "$t/models" else "$t/v1/models"
    }

    /**
     * 解析模型列表，兼容常见返回结构：
     * - {"data": [{"id": "gpt-4o"}, ...]}        OpenAI / Anthropic 官方格式
     * - {"models": [{"name": "llama3"}, ...]}    部分中转站 / 本地服务
     * - ["gpt-4o", ...]                           直接返回字符串数组
     */
    private fun parseModels(body: String): List<String> {
        return try {
            val trimmed = body.trim()
            val arr = if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else {
                val obj = JSONObject(trimmed)
                obj.optJSONArray("data") ?: obj.optJSONArray("models") ?: JSONArray()
            }
            (0 until arr.length()).mapNotNull { i ->
                when (val el = arr.get(i)) {
                    is String -> el
                    is JSONObject -> {
                        val id = el.optString("id")
                            .ifEmpty { el.optString("name") }
                            .ifEmpty { el.optString("model") }
                        id.ifEmpty { null }
                    }
                    else -> null
                }
            }.filter { it.isNotBlank() }
                .distinct()
                .sorted()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
