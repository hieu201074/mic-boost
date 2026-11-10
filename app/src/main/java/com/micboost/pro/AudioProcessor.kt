
package com.micboost.pro

import kotlin.math.abs
import kotlin.math.tanh

class AudioProcessor {

    @Volatile
    var gain: Float = 2.0f

    @Volatile
    var compressorEnabled: Boolean = true

    fun process(
        input: ShortArray,
        count: Int
    ): ShortArray {

        val output = ShortArray(count)

        for (i in 0 until count) {

            var sample = input[i] / 32768f

            // Gain Boost
            sample *= gain

            // Compressor
            if (compressorEnabled) {
                val level = abs(sample)
                val threshold = 0.65f
                val ratio = 4f

                if (level > threshold) {
                    val excess = level - threshold
                    val compressed =
                        threshold + excess / ratio

                    sample =
                        if (sample >= 0f) compressed
                        else -compressed
                }
            }

            // Soft limiter
            sample = tanh(sample * 1.2f) / tanh(1.2f)

            val value = (sample * 32767f)
                .toInt()
                .coerceIn(-32768, 32767)

            output[i] = value.toShort()
        }

        return output
    }
}
