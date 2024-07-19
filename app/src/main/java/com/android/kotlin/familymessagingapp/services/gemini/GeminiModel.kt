package com.android.kotlin.familymessagingapp.services.gemini

import android.app.Application
import android.graphics.Bitmap
import com.android.kotlin.familymessagingapp.BuildConfig
import com.android.kotlin.familymessagingapp.model.Message
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiModel(private val application: Application) {

    private val model: GenerativeModel =
        GenerativeModel(
            BuildConfig.geminiModelName,
            // Retrieve API key as an environmental variable defined in a Build Configuration
            // see https://github.com/google/secrets-gradle-plugin for further instructions
            // TODO: Add Gemini API key as an environmental variable
            BuildConfig.geminiApiKey,
            generationConfig = generationConfig {
                temperature = 1f
                topK = 64
                topP = 0.95f
                maxOutputTokens = 8192
                responseMimeType = "text/plain"
            },
            // safetySettings = Adjust safety settings
            // See https://ai.google.dev/gemini-api/docs/safety-settings
        )

    suspend fun generateContent(message: Message): String? {
        return withContext(Dispatchers.IO) {
            try {
                val photoUrl = message.photo
                val text = message.text
                var photoBitmap: Bitmap? = null
//                if (photoUrl != null)
//                    photoBitmap = AppImageUtils.convertImageUrlToBitmap(application, photoUrl)
                if (photoBitmap == null && text.isNullOrEmpty()) return@withContext null
                val content = content {
//                    if (photoBitmap != null) {
//                        image(photoBitmap)
//                    }
                    if (!text.isNullOrEmpty()) text(text)
                }
                // Assuming a method generate exists that takes a map and returns a response
                val response = model.generateContent(content)
                response.text // Assuming the response contains generatedText
            } catch (e: Exception) {
                null
            }
        }
    }
}