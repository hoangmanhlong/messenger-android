package com.android.kotlin.familymessagingapp.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object TimeUtils {

    /**
     * Countdown function
     * @param countDownTime time countdown in seconds
     * @param onFinish callback when countdown finishes
     */
    fun startCountdown(
        scope: CoroutineScope = CoroutineScope(Job() + Dispatchers.Main),
        countDownTime: Int,
        delay: Long = 1000L,
        onFinish: () -> Unit
    ) {
        var timeRemaining = countDownTime
        scope.launch {
            while (timeRemaining > 0) {
                delay(delay) // Wait for 1 second
                timeRemaining--
            }
            onFinish() // Callback when countdown finishes
        }
    }
}