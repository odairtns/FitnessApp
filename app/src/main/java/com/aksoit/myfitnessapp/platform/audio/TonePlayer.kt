package com.aksoit.myfitnessapp.platform.audio

import android.media.AudioManager
import android.media.ToneGenerator

class TonePlayer {

    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 85)
    } catch (_: Exception) {
        null
    }

    fun playTick() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
    }

    fun playWarning() {
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150)
    }

    fun playStart() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 350)
    }

    fun playFinish() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 500)
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
