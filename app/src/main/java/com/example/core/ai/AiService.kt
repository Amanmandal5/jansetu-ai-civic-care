package com.example.core.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.entity.IssueEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

// --- AI Service Result Models ---

data class AiAnalysisResult(
    val category: String,
    val confidenceScore: Double,
    val severity: String, // Low, Medium, High, Critical
    val severityScore: Int, // 0 - 100
    val trustScore: Int, // 0 - 100
    val suggestedDepartment: String,
    val explanation: String,
    val duplicateWarning: Boolean = false
)

data class DuplicateCheckResult(
    val duplicateFound: Boolean,
    val duplicateIssueId: String? = null,
    val duplicateScore: Double = 0.0,
    val suggestedAction: String? = null
)

data class SeverityResult(
    val severityLevel: String,
    val severityScore: Int,
    val reason: String
)

data class TrustScoreResult(
    val trustScore: Int,
    val fakeProbability: Double,
    val reviewRequired: Boolean
)

// --- AI Service Interface ---

interface AiService {
    suspend fun analyzeIssue(title: String, description: String, imageBase64: String?): AiAnalysisResult
    suspend fun checkDuplicate(title: String, description: String, lat: Double, lon: Double, existingIssues: List<IssueEntity>): DuplicateCheckResult
}

// --- Mock AI Service Implementation ---

class MockAiService : AiService {
    override suspend fun analyzeIssue(title: String, description: String, imageBase64: String?): AiAnalysisResult = withContext(Dispatchers.Default) {
        val fullText = (title + " " + description).lowercase()
        
        var category = "Other"
        var department = "General Civic Services"
        var severity = "Medium"
        var severityScore = 45
        var explanation = "Based on text cues, this issue needs manual review."

        when {
            fullText.contains("pothole") || fullText.contains("cracked road") || fullText.contains("crater") -> {
                category = "Pothole"
                department = "Roads & Highways Department"
                severity = "High"
                severityScore = 75
                explanation = "Image and description show clear road hazards and deep holes, risking vehicle damage and rider safety."
            }
            fullText.contains("garbage") || fullText.contains("trash") || fullText.contains("waste") || fullText.contains("dumped") -> {
                category = "Garbage Overflow"
                department = "Waste & Sanitation Department"
                severity = "Medium"
                severityScore = 55
                explanation = "Debris accumulation detected. High probability of health risk and offensive odor."
            }
            fullText.contains("streetlight") || fullText.contains("dark street") || fullText.contains("bulb broken") -> {
                category = "Broken Streetlight"
                department = "Electrical Infrastructure Dept"
                severity = "Medium"
                severityScore = 50
                explanation = "Light source failure detected, reducing pedestrian safety and increasing security vulnerabilities."
            }
            fullText.contains("leak") || fullText.contains("water pipe") || fullText.contains("burst") -> {
                category = "Water Leakage"
                department = "Municipal Water & Sewerage Board"
                severity = "High"
                severityScore = 70
                explanation = "Active water discharge reported. Water wastage and localized flooding probability is high."
            }
            fullText.contains("drain") || fullText.contains("clog") || fullText.contains("sewer") -> {
                category = "Drainage Blockage"
                department = "Municipal Water & Sewerage Board"
                severity = "High"
                severityScore = 72
                explanation = "Sewer system overflow risk. Can cause backflow, heavy street logging, and hygiene issues."
            }
            fullText.contains("tree") || fullText.contains("branch") || fullText.contains("fallen") -> {
                category = "Fallen Tree"
                department = "Forestry & Parks Department"
                severity = "Medium"
                severityScore = 58
                explanation = "Obstruction on public pathway. Requires prompt removal to clear the lane."
            }
            fullText.contains("signal") || fullText.contains("traffic light") -> {
                category = "Traffic Signal Issue"
                department = "Traffic Control Department"
                severity = "Critical"
                severityScore = 90
                explanation = "Broken traffic lights cause major congestion and safety hazards at busy intersections."
            }
            fullText.contains("unsafe") || fullText.contains("dark lane") || fullText.contains("shady") -> {
                category = "Unsafe Public Area"
                department = "City Security & Policing"
                severity = "High"
                severityScore = 80
                explanation = "Citizen security threat identified due to lack of patrol or light conditions."
            }
            fullText.contains("toilet") || fullText.contains("washroom") -> {
                category = "Public Toilet Issue"
                department = "Sanitation Department"
                severity = "Medium"
                severityScore = 40
                explanation = "Public health and sanitation amenities are in bad condition."
            }
        }

        AiAnalysisResult(
            category = category,
            confidenceScore = 0.92,
            severity = severity,
            severityScore = severityScore,
            trustScore = 95,
            suggestedDepartment = department,
            explanation = explanation,
            duplicateWarning = false
        )
    }

    override suspend fun checkDuplicate(
        title: String,
        description: String,
        lat: Double,
        lon: Double,
        existingIssues: List<IssueEntity>
    ): DuplicateCheckResult = withContext(Dispatchers.Default) {
        // Simple distance calculation (Haversine formula placeholder)
        for (issue in existingIssues) {
            val dist = calculateDistance(lat, lon, issue.latitude, issue.longitude)
            // If less than 100 meters and category match, check duplicate similarity
            if (dist < 100.0) {
                val titleSimilarity = if (issue.title.lowercase().contains(title.lowercase()) || title.lowercase().contains(issue.title.lowercase())) 0.8 else 0.4
                val catSimilarity = if (issue.category.equals(issue.category, ignoreCase = true)) 1.0 else 0.0
                
                val locationSimilarity = 0.95
                val imageSimilarity = 0.70
                val textSimilarity = titleSimilarity
                val categoryMatch = catSimilarity

                val duplicateScore = (0.40 * locationSimilarity) + (0.30 * imageSimilarity) + (0.20 * textSimilarity) + (0.10 * categoryMatch)
                if (duplicateScore > 0.70) {
                    return@withContext DuplicateCheckResult(
                        duplicateFound = true,
                        duplicateIssueId = issue.id,
                        duplicateScore = duplicateScore,
                        suggestedAction = "Join this existing report instead of creating a new ticket. It was reported ${calculateMinutesAgo(issue.createdAt)} minutes ago."
                    )
                }
            }
        }
        DuplicateCheckResult(duplicateFound = false)
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371e3 // meters
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) *
                Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return r * c // meters
    }

    private fun calculateMinutesAgo(createdAt: Long): Int {
        val diff = System.currentTimeMillis() - createdAt
        return (diff / (1000 * 60)).toInt().coerceAtLeast(1)
    }
}

// --- Gemini AI Service Implementation ---

class GeminiAiService : AiService {
    private val mockFallback = MockAiService()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun analyzeIssue(title: String, description: String, imageBase64: String?): AiAnalysisResult {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            Log.d("GeminiAiService", "API Key is placeholder. Falling back to MockAiService.")
            return mockFallback.analyzeIssue(title, description, imageBase64)
        }

        return withContext(Dispatchers.IO) {
            try {
                val textPrompt = """
                    Analyze this civic issue report and provide a JSON response.
                    Title: "$title"
                    Description: "$description"
                    
                    Respond with a valid JSON object matching this schema exactly:
                    {
                      "category": "One of: Pothole, Road Damage, Water Leakage, Broken Streetlight, Garbage Overflow, Drainage Blockage, Damaged Public Property, Fallen Tree, Traffic Signal Issue, Unsafe Public Area, Public Toilet Issue, Other",
                      "confidenceScore": 0.0 to 1.0,
                      "severity": "One of: Low, Medium, High, Critical",
                      "severityScore": 0 to 100,
                      "trustScore": 0 to 100,
                      "suggestedDepartment": "Name of department responsible for fixing this",
                      "explanation": "Short sentence explaining why this category and severity were chosen"
                    }
                """.trimIndent()

                val requestJson = """
                    {
                      "contents": [
                        {
                          "parts": [
                            { "text": ${JSONObject.quote(textPrompt)} }
                            ${if (imageBase64 != null) ", { \"inlineData\": { \"mimeType\": \"image/jpeg\", \"data\": \"$imageBase64\" } }" else ""}
                          ]
                        }
                      ],
                      "generationConfig": {
                        "responseMimeType": "application/json"
                      }
                    }
                """.trimIndent()

                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val body = requestJson.toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("GeminiAiService", "API call failed with code ${response.code}")
                        return@withContext mockFallback.analyzeIssue(title, description, imageBase64)
                    }

                    val responseString = response.body?.string() ?: ""
                    val jsonResponse = JSONObject(responseString)
                    val candidates = jsonResponse.getJSONArray("candidates")
                    val text = candidates.getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")

                    val resultJson = JSONObject(text.trim())
                    AiAnalysisResult(
                        category = resultJson.optString("category", "Other"),
                        confidenceScore = resultJson.optDouble("confidenceScore", 0.90),
                        severity = resultJson.optString("severity", "Medium"),
                        severityScore = resultJson.optInt("severityScore", 50),
                        trustScore = resultJson.optInt("trustScore", 95),
                        suggestedDepartment = resultJson.optString("suggestedDepartment", "Municipal Corporation"),
                        explanation = resultJson.optString("explanation", "Analyzed using Gemini AI."),
                        duplicateWarning = false
                    )
                }
            } catch (e: Exception) {
                Log.e("GeminiAiService", "Error during Gemini call: ${e.message}", e)
                mockFallback.analyzeIssue(title, description, imageBase64)
            }
        }
    }

    override suspend fun checkDuplicate(
        title: String,
        description: String,
        lat: Double,
        lon: Double,
        existingIssues: List<IssueEntity>
    ): DuplicateCheckResult {
        return mockFallback.checkDuplicate(title, description, lat, lon, existingIssues)
    }
}
