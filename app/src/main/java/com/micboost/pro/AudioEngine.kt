
package com.micboost.pro

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.AudioManager
import kotlin.concurrent.thread

class AudioEngine {

    companion object {
        private const val SAMPLE_RATE = 44100
    }

    private var recorder: AudioRecord? = null
    private var player: AudioTrack? = null
    private var worker: Thread? = null

    private val processor = AudioProcessor()

    @Volatile
    private var running = false

    val isRunning: Boolean
        get() = running

    fun setGain(value: Float) {
        processor.gain = value
    }

    fun start() {
        if (running) return

        val minBuffer = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        if (minBuffer <= 0) {
            throw IllegalStateException("Microphone unavailable")
        }

        val bufferSize = minBuffer * 2

        val input = AudioRecord.Builder()
            .setAudioSource(
                MediaRecorder.AudioSource.MIC
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(
                        AudioFormat.CHANNEL_IN_MONO
                    )
                    .setEncoding(
                        AudioFormat.ENCODING_PCM_16BIT
                    )
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .build()

        val output = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(
                        AudioAttributes.USAGE_MEDIA
                    )
                    .setContentType(
                        AudioAttributes.CONTENT_TYPE_SPEECH
                    )
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(
                        AudioFormat.CHANNEL_OUT_MONO
                    )
                    .setEncoding(
                        AudioFormat.ENCODING_PCM_16BIT
                    )
                    .build()
            )
            .setTransferMode(
                AudioTrack.MODE_STREAM
            )
            .setBufferSizeInBytes(bufferSize)
            .build()

        recorder = input
        player = output

        try {
            input.startRecording()
            output.play()
            running = true

            worker = thread(
                start = true,
                name = "MicBoostAudio"
            ) {
                val buffer = ShortArray(1024)

                while (running) {
                    val read = input.read(
                        buffer,
                        0,
                        buffer.size
                    )

                    if (read > 0) {
                        val processed =
                            processor.process(buffer, read)

                        output.write(
                            processed,
                            0,
                            processed.size
                        )
                    } else if (read < 0) {
                        break
                    }
                }
            }

        } catch (e: Exception) {
            stop()
            throw e
        }
    }

    fun stop() {
        running = false

        try {
            recorder?.stop()
        } catch (_: Exception) {
        }

        try {
            player?.pause()
            player?.flush()
            player?.stop()
        } catch (_: Exception) {
        }

        worker?.join(500)

        recorder?.release()
        player?.release()

        recorder = null
        player = null
        worker = null
    }
}
