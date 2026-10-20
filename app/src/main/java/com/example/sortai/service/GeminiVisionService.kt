package com.example.sortai.service

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.sortai.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiVisionService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun classifyImage(
        bitmap: Bitmap,
        mode: ScanMode
    ): Result<InferenceResult> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(IllegalStateException("Gemini API key is not configured in Secrets panel"))
        }

        val startTime = System.currentTimeMillis()
        try {
            // Compress bitmap to JPEG Base64
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            val prompt = """
                Analyze this electronic waste item for safe recycling and sorting into one of these EXACT categories:
                1. "PCB" (Printed circuit boards from phones, PCs, appliances)
                2. "BATTERY" (Lithium-ion, lead-acid, cells, swollen battery)
                3. "COPPER_MOTOR" (Copper coils, transformers, electric motors)
                4. "CABLE" (Cables, wires, wiring harnesses)
                5. "DISPLAY" (TV panels, monitors, LCD, CRT, cracked screens)
                6. "PLASTIC_GLASS" (Engineered casing plastics, acrylic, glass)
                7. "UNKNOWN_REVIEW" (Uncertain or mixed items needing review)

                Scan Mode: ${mode.name}

                Respond ONLY in strict JSON format matching this schema:
                {
                   "category": "PCB | BATTERY | COPPER_MOTOR | CABLE | DISPLAY | PLASTIC_GLASS | UNKNOWN_REVIEW",
                   "confidence": 0.0 to 1.0,
                   "detectedLabel": "short descriptive name of object",
                   "isHazard": boolean,
                   "hazardLevel": "NONE | LOW | MEDIUM | CRITICAL",
                   "hazardWarning": "clear safety instruction in English",
                   "hazardWarningHindi": "clear safety instruction in Hindi",
                   "components": [
                      {"name": "component name", "category": "category name", "sharePercent": 10 to 100}
                   ],
                   "requiresHumanReview": boolean
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                            put(JSONObject().apply {
                                put("inline_data", JSONObject().apply {
                                    put("mime_type", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)

                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("responseMimeType", "application/json")
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiVisionService", "HTTP ${response.code}: $responseBody")
                return@withContext Result.failure(Exception("Gemini API error ${response.code}"))
            }

            val latency = System.currentTimeMillis() - startTime
            val result = parseGeminiResponse(responseBody, mode, latency)
            Result.success(result)
        } catch (e: Exception) {
            Log.e("GeminiVisionService", "Exception in Gemini Vision", e)
            Result.failure(e)
        }
    }

    private fun parseGeminiResponse(jsonText: String, mode: ScanMode, latencyMs: Long): InferenceResult {
        val root = JSONObject(jsonText)
        val candidates = root.optJSONArray("candidates")
        val firstCandidate = candidates?.optJSONObject(0)
        val content = firstCandidate?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val text = parts?.optJSONObject(0)?.optString("text") ?: "{}"

        val cleanJson = if (text.contains("```json")) {
            text.substringAfter("```json").substringBefore("```").trim()
        } else if (text.contains("```")) {
            text.substringAfter("```").substringBefore("```").trim()
        } else {
            text.trim()
        }

        val parsed = JSONObject(cleanJson)
        val catName = parsed.optString("category", "UNKNOWN_REVIEW")
        val category = EWasteCategory.fromName(catName)
        val confidence = parsed.optDouble("confidence", 0.85).toFloat()
        val detectedLabel = parsed.optString("detectedLabel", category.englishName)
        val isHazard = parsed.optBoolean("isHazard", category.isHazardous)
        val hazardLevelStr = parsed.optString("hazardLevel", if (isHazard) "MEDIUM" else "NONE")
        val hazardLevel = try {
            HazardLevel.valueOf(hazardLevelStr)
        } catch (e: Exception) {
            if (isHazard) HazardLevel.MEDIUM else HazardLevel.NONE
        }

        val hazardWarning = parsed.optString("hazardWarning", category.safeHandlingEnglish)
        val hazardWarningHindi = parsed.optString("hazardWarningHindi", category.safeHandlingHindi)
        val requiresReview = parsed.optBoolean("requiresHumanReview", confidence < 0.70f || isHazard)

        val componentsList = mutableListOf<ComponentBreakdown>()
        val compArr = parsed.optJSONArray("components")
        if (compArr != null) {
            for (i in 0 until compArr.length()) {
                val item = compArr.optJSONObject(i) ?: continue
                val cName = item.optString("name", "Component")
                val cCat = EWasteCategory.fromName(item.optString("category", "PCB"))
                val cShare = item.optInt("sharePercent", 50)
                componentsList.add(ComponentBreakdown(cName, cCat, cShare))
            }
        }

        return InferenceResult(
            category = category,
            confidence = confidence,
            detectedLabel = detectedLabel,
            mode = mode,
            isHazard = isHazard,
            hazardLevel = hazardLevel,
            hazardWarning = hazardWarning,
            hazardWarningHindi = hazardWarningHindi,
            components = componentsList,
            requiresHumanReview = requiresReview,
            inferenceSource = InferenceSource.GEMINI_3_5_FLASH,
            modelLatencyMs = latencyMs,
            recommendedCrate = category
        )
    }
}
