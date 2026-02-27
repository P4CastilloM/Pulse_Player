package com.example.pulseplayer.views.player

import android.media.audiofx.Equalizer
import androidx.media3.exoplayer.ExoPlayer

class PlayerEqualizer(private val playerProvider: () -> ExoPlayer?) {
    private var equalizer: Equalizer? = null
    private var boundSessionId: Int? = null

    fun ensureReady(): Boolean {
        val player = playerProvider() ?: return false
        val audioSessionId = player.audioSessionId
        if (audioSessionId <= 0) return false

        if (boundSessionId == audioSessionId && equalizer != null) return true

        release()
        return try {
            equalizer = Equalizer(0, audioSessionId).apply { enabled = true }
            boundSessionId = audioSessionId
            true
        } catch (_: Exception) {
            equalizer = null
            boundSessionId = null
            false
        }
    }

    fun bandLevelRange(): ClosedRange<Short> {
        if (!ensureReady()) return (-1500).toShort()..1500.toShort()
        val range = equalizer?.bandLevelRange
        val min = range?.getOrNull(0) ?: (-1500).toShort()
        val max = range?.getOrNull(1) ?: 1500.toShort()
        return min..max
    }

    fun bandCount(): Int {
        if (!ensureReady()) return 0
        return equalizer?.numberOfBands?.toInt() ?: 0
    }

    fun bandLevel(index: Int): Short {
        if (!ensureReady()) return 0
        return equalizer?.getBandLevel(index.toShort()) ?: 0
    }

    fun setBandLevel(index: Int, value: Short) {
        if (!ensureReady()) return
        equalizer?.setBandLevel(index.toShort(), value)
    }

    fun centerFreqHz(index: Int): Int {
        if (!ensureReady()) return 0
        return (equalizer?.getCenterFreq(index.toShort()) ?: 0) / 1000
    }

    fun presets(): List<Pair<Short, String>> {
        if (!ensureReady()) return emptyList()
        val eq = equalizer ?: return emptyList()
        val presetCount = eq.numberOfPresets.toInt()
        return (0 until presetCount).map { index ->
            val presetId = index.toShort()
            presetId to eq.getPresetName(presetId)
        }
    }

    fun currentPreset(): Short {
        if (!ensureReady()) return CUSTOM_PRESET
        val eq = equalizer ?: return CUSTOM_PRESET
        return try {
            eq.currentPreset
        } catch (_: Exception) {
            CUSTOM_PRESET
        }
    }

    fun usePreset(preset: Short) {
        if (!ensureReady()) return
        if (preset == CUSTOM_PRESET) return
        equalizer?.usePreset(preset)
    }

    fun release() {
        equalizer?.release()
        equalizer = null
        boundSessionId = null
    }

    companion object {
        const val CUSTOM_PRESET: Short = -1
    }
}
