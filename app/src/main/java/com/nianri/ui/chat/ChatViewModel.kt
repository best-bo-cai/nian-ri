package com.nianri.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nianri.data.entity.AiConfigEntity
import com.nianri.data.entity.EventEntity
import com.nianri.data.repository.EventRepository
import com.nianri.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val isUser: Boolean,
    val content: String,
    val parsedEvents: List<EventEntity>? = null,
    val isError: Boolean = false,
    val originalUserMessage: String? = null
)

class ChatViewModel(private val repository: EventRepository) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _aiConfigured = MutableStateFlow(false)
    val aiConfigured: StateFlow<Boolean> = _aiConfigured.asStateFlow()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    init {
        checkAiConfig()
    }

    private fun checkAiConfig() {
        viewModelScope.launch {
            repository.getAiConfigs().collect { configs ->
                _aiConfigured.value = configs.any { it.isActive }
            }
        }
    }

    fun sendMessage(text: String) {
        if (!_aiConfigured.value) {
            _messages.value = _messages.value + ChatMessage(
                isUser = true,
                content = text
            ) + ChatMessage(
                isUser = false,
                content = "请先在\"我的\"页面配置 AI 大模型",
                isError = true,
                originalUserMessage = text
            )
            return
        }

        _messages.value = _messages.value + ChatMessage(isUser = true, content = text)
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val config = repository.getActiveAiConfig()
                if (config == null) {
                    addAssistantMessage("AI 配置无效，请重新配置", isError = true, originalUserMessage = text)
                    _isLoading.value = false
                    return@launch
                }

                val result = parseWithAI(text, config)
                result.fold(
                    onSuccess = { events ->
                        if (events.isNotEmpty()) {
                            _messages.value = _messages.value + ChatMessage(
                                isUser = false,
                                content = "已识别到以下日子/事件：",
                                parsedEvents = events
                            )
                        } else {
                            addAssistantMessage("是否有什么日子或事件需要帮你记录？")
                        }
                    },
                    onFailure = { error ->
                        addAssistantMessage("解析失败：${error.message}", isError = true, originalUserMessage = text)
                    }
                )
            } catch (e: Exception) {
                addAssistantMessage("解析失败：${e.message}", isError = true, originalUserMessage = text)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun retryMessage(messageId: Long) {
        val message = _messages.value.find { it.id == messageId } ?: return
        val originalText = message.originalUserMessage ?: return

        _messages.value = _messages.value.filter { it.id != messageId }
        sendMessage(originalText)
    }

    private fun addAssistantMessage(content: String, isError: Boolean = false, originalUserMessage: String? = null) {
        _messages.value = _messages.value + ChatMessage(
            isUser = false,
            content = content,
            isError = isError,
            originalUserMessage = originalUserMessage
        )
    }

    private suspend fun parseWithAI(
        text: String,
        config: AiConfigEntity
    ): Result<List<EventEntity>> = withContext(Dispatchers.IO) {
        try {
            val isAnthropic = config.provider == "anthropic"
            val url = buildRequestUrl(config.baseUrl, isAnthropic)
            val requestBody = if (isAnthropic) {
                buildAnthropicRequestBody(text, config.model)
            } else {
                buildOpenAiRequestBody(text, config.model)
            }
            val request = if (isAnthropic) {
                buildAnthropicRequest(url, config.apiKey, requestBody)
            } else {
                buildOpenAiRequest(url, config.apiKey, requestBody)
            }

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = parseErrorResponse(responseBody, isAnthropic)
                return@withContext Result.failure(Exception("API 返回错误 (${response.code})：$errorMsg"))
            }

            val events = if (isAnthropic) {
                parseAnthropicResponse(responseBody)
            } else {
                parseOpenAiResponse(responseBody)
            }

            Result.success(events)
        } catch (e: Exception) {
            if (e.message?.contains("API 返回错误") == true) {
                Result.failure(e)
            } else {
                Result.failure(Exception("网络请求失败：${e.message}"))
            }
        }
    }

    private fun buildRequestUrl(baseUrl: String, isAnthropic: Boolean): String {
        val trimmed = baseUrl.trimEnd('/')

        if (isAnthropic) {
            if (trimmed.endsWith("/v1/messages") || trimmed.endsWith("/messages")) {
                return trimmed
            }
            if (trimmed.endsWith("/v1")) {
                return "$trimmed/messages"
            }
            return "$trimmed/v1/messages"
        }

        if (trimmed.endsWith("/v1/chat/completions") || trimmed.endsWith("/chat/completions")) {
            return trimmed
        }
        if (trimmed.endsWith("/v1")) {
            return "$trimmed/chat/completions"
        }
        return "$trimmed/v1/chat/completions"
    }

    private fun buildOpenAiRequestBody(text: String, model: String): String {
        val systemPrompt = buildSystemPrompt()
        return JSONObject().apply {
            put("model", model)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", text)
                })
            })
            put("temperature", 0.3)
            put("max_tokens", 2000)
        }.toString()
    }

    private fun buildAnthropicRequestBody(text: String, model: String): String {
        val systemPrompt = buildSystemPrompt()
        return JSONObject().apply {
            put("model", model)
            put("max_tokens", 2000)
            put("system", systemPrompt)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", text)
                })
            })
        }.toString()
    }

    private fun buildSystemPrompt(): String {
        val today = DateUtils.formatDate(System.currentTimeMillis())
        return """你是一个日期解析助手。用户会用中文描述日子或事件，你需要从中提取信息并返回 JSON 数组。
每个事件包含以下字段：
- name: 事件名称（字符串）
- type: 类型，只能是 "birthday"、"anniversary"、"holiday"、"event" 之一
- calendarType: 日历类型，"solar"（阳历）或 "lunar"（农历），默认 "solar"
- date: 日期，格式 "yyyy-MM-dd"，如果用户说"下周五"等相对日期，请计算具体日期。当前日期是 $today
- endDate: 结束日期（仅事件型需要），格式 "yyyy-MM-dd"，不需要时省略该字段
- displayMode: "countdown"（倒计时）或 "countup"（正计时），默认 "countdown"
- repeatRule: "none"、"yearly"、"monthly"、"weekly" 之一，默认 "none"
- notes: 备注信息，可为空字符串

请只返回 JSON 数组，不要包含其他文字。如果没有识别到任何事件，返回空数组 []。"""
    }

    private fun buildOpenAiRequest(url: String, apiKey: String, body: String): Request {
        return Request.Builder()
            .url(url)
            .post(body.toRequestBody("application/json".toMediaType()))
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()
    }

    private fun buildAnthropicRequest(url: String, apiKey: String, body: String): Request {
        return Request.Builder()
            .url(url)
            .post(body.toRequestBody("application/json".toMediaType()))
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("Content-Type", "application/json")
            .build()
    }

    private fun parseOpenAiResponse(responseBody: String): List<EventEntity> {
        val jsonResponse = JSONObject(responseBody)
        val choices = jsonResponse.getJSONArray("choices")
        if (choices.length() == 0) return emptyList()

        val message = choices.getJSONObject(0).getJSONObject("message")
        val content = message.getString("content")
        return parseEventsFromContent(content)
    }

    private fun parseAnthropicResponse(responseBody: String): List<EventEntity> {
        val jsonResponse = JSONObject(responseBody)
        val contentArray = jsonResponse.getJSONArray("content")
        if (contentArray.length() == 0) return emptyList()

        val textContent = contentArray.getJSONObject(0).getString("text")
        return parseEventsFromContent(textContent)
    }

    private fun parseEventsFromContent(content: String): List<EventEntity> {
        val cleanedContent = content.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        if (cleanedContent.isEmpty() || cleanedContent == "[]") return emptyList()

        return try {
            val eventsArray = JSONArray(cleanedContent)
            val events = mutableListOf<EventEntity>()
            for (i in 0 until eventsArray.length()) {
                val obj = eventsArray.getJSONObject(i)
                val dateStr = obj.optString("date", DateUtils.formatDate(System.currentTimeMillis()))
                val endDateStr = if (obj.has("endDate") && !obj.isNull("endDate")) {
                    obj.optString("endDate", "")
                } else {
                    ""
                }

                events.add(
                    EventEntity(
                        type = obj.optString("type", "event"),
                        name = obj.optString("name", "未命名"),
                        calendarType = obj.optString("calendarType", "solar"),
                        date = DateUtils.parseDate(dateStr),
                        endDate = endDateStr.takeIf { it.isNotEmpty() }?.let { DateUtils.parseDate(it) },
                        displayMode = obj.optString("displayMode", "countdown"),
                        repeatRule = obj.optString("repeatRule", "none"),
                        notes = obj.optString("notes", "")
                    )
                )
            }
            events
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseErrorResponse(responseBody: String, isAnthropic: Boolean): String {
        return try {
            val json = JSONObject(responseBody)
            if (isAnthropic) {
                json.optJSONObject("error")?.optString("message")
                    ?: json.optString("message", "未知错误")
            } else {
                json.optJSONObject("error")?.optString("message")
                    ?: json.optString("message", "未知错误")
            }
        } catch (e: Exception) {
            responseBody.take(200)
        }
    }

    fun confirmCreateEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.insertEvent(event)
            _messages.value = _messages.value + ChatMessage(
                isUser = false,
                content = "已创建：${event.name}"
            )
        }
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    class Factory(private val repository: EventRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(repository) as T
        }
    }
}
