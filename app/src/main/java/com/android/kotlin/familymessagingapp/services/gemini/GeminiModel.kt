package com.android.kotlin.familymessagingapp.services.gemini

import com.android.kotlin.familymessagingapp.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig

class GeminiModel {

    val model: GenerativeModel =
        GenerativeModel(
            BuildConfig.GEMINI_MODEL_NAME,
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

}