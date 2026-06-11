package com.sss.healthcare

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

private const val OPENAI_API_KEY_FOR_MVP = ""
private const val OPENAI_RESPONSES_URL = "https://api.openai.com/v1/responses"
private const val OPENAI_MODEL = "gpt-4.1-mini"

data class InsightInput(
    val sleepHours: List<Float>,
    val exerciseMinutes: List<Float>,
    val diseaseScores: List<Float>,
    val selectedDisease: String
)

class AiInsightService(
    private val apiKey: String = BuildConfig.OPENAI_API_KEY.ifBlank { OPENAI_API_KEY_FOR_MVP },
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    suspend fun createInsight(input: InsightInput): String {
        return runCatching {
            require(apiKey.isNotBlank()) { "OpenAI API key is empty." }
            requestOpenAiInsight(input)
        }.getOrElse {
            fallbackInsight(input)
        }
    }

    private suspend fun requestOpenAiInsight(input: InsightInput): String = withContext(Dispatchers.IO) {
        val jsonBody = JSONObject()
            .put("model", OPENAI_MODEL)
            .put("max_output_tokens", 240)
            .put(
                "input",
                JSONArray()
                    .put(
                        JSONObject()
                            .put("role", "system")
                            .put("content", INSIGHT_SYSTEM_PROMPT)
                    )
                    .put(
                        JSONObject()
                            .put("role", "user")
                            .put("content", input.toPromptData())
                    )
            )

        val request = Request.Builder()
            .url(OPENAI_RESPONSES_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("OpenAI request failed: ${response.code}")
            }

            val body = response.body?.string().orEmpty()
            val parsed = JSONObject(body)
            parsed.optString("output_text").ifBlank {
                parsed.extractResponseText()
            }.ifBlank {
                throw IllegalStateException("OpenAI response text is empty.")
            }.cleanInsightText()
        }
    }
}

fun fallbackInsight(input: InsightInput): String {
    val sleepAverage = input.sleepHours.averageOrNull()
    val exerciseAverage = input.exerciseMinutes.averageOrNull()
    val scoreRange = input.diseaseScores.let { scores ->
        if (scores.isEmpty()) 0f else (scores.maxOrNull() ?: 0f) - (scores.minOrNull() ?: 0f)
    }

    val sleepText = if (sleepAverage >= 6.5f) {
        "최근 기록상 수면 시간은 비교적 안정적으로 기록되었어요."
    } else {
        "입력된 기록 기준으로 수면 시간이 짧게 기록된 날이 있었어요."
    }

    val exerciseText = if (exerciseAverage >= 30f) {
        "참고로 운동 시간은 여러 날짜에 꾸준히 남아 있어요."
    } else {
        "운동 시간은 일부 날짜에 부족하게 기록되었어요."
    }

    val scoreText = if (scoreRange <= 12f) {
        "질환 상태점수는 최근 며칠간 큰 변동 없이 유지되고 있어요."
    } else {
        "질환 상태점수는 날짜별 차이가 있어 기록 흐름을 함께 살펴보면 좋아요."
    }

    return listOf(sleepText, exerciseText, scoreText).joinToString("\n")
}

private fun InsightInput.toPromptData(): String {
    return """
        선택 질환: $selectedDisease
        최근 7일 수면 시간: ${sleepHours.joinToString(", ") { "${formatNumber(it)}시간" }}
        최근 7일 운동 시간: ${exerciseMinutes.joinToString(", ") { "${it.roundToInt()}분" }}
        최근 7일 질환 상태점수: ${diseaseScores.joinToString(", ") { "${it.roundToInt()}점" }}
    """.trimIndent()
}

private fun JSONObject.extractResponseText(): String {
    val output = optJSONArray("output") ?: return ""
    val builder = StringBuilder()

    for (i in 0 until output.length()) {
        val item = output.optJSONObject(i) ?: continue
        val content = item.optJSONArray("content") ?: continue
        for (j in 0 until content.length()) {
            val contentItem = content.optJSONObject(j) ?: continue
            val text = contentItem.optString("text")
            if (text.isNotBlank()) {
                if (builder.isNotEmpty()) builder.append("\n")
                builder.append(text)
            }
        }
    }

    return builder.toString()
}

private fun String.cleanInsightText(): String {
    return lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .take(3)
        .joinToString("\n")
}

private fun List<Float>.averageOrNull(): Float {
    return if (isEmpty()) 0f else average().toFloat()
}

private fun formatNumber(value: Float): String {
    return if (value % 1f == 0f) value.roundToInt().toString() else String.format(java.util.Locale.US, "%.1f", value)
}

private const val INSIGHT_SYSTEM_PROMPT = """
너는 의료 진단을 하지 않는 건강 기록 요약 도우미다.
사용자의 최근 7일 수면 시간, 운동 시간, 질환 상태점수 기록을 바탕으로 오늘의 인사이트를 작성하라.
진단, 치료 판단, 약 조절, 정상/비정상 판정은 하지 마라.
사용자가 이해하기 쉬운 부드러운 한국어로 2~3개의 짧은 인사이트를 작성하라.
각 문장은 기록 기반 참고 정보라는 느낌이 나야 한다.
금지 표현: 정상, 위험, 치료, 약 조절, 병이 좋아짐, 악화됨.
권장 표현: 기록상, 참고로, 입력된 기록 기준.
증상이 지속되거나 불편감이 있으면 의료진 상담을 고려하라는 문구는 허용한다.
"""
