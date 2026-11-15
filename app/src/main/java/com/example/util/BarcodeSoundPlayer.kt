package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.R
import java.util.concurrent.atomic.AtomicBoolean

class BarcodeSoundPlayer(context: Context) {
  private val appContext = context.applicationContext
  private val isLoaded = AtomicBoolean(false)
  private val isReleased = AtomicBoolean(false)

  private val soundPool: SoundPool =
    SoundPool.Builder()
      .setMaxStreams(2)
      .setAudioAttributes(
        AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build()
      )
      .build()

  private val beepSoundId: Int

  init {
    soundPool.setOnLoadCompleteListener { _, sampleId, status ->
      if (status == 0 && sampleId == beepSoundId) {
        isLoaded.set(true)
      }
    }
    beepSoundId = soundPool.load(appContext, R.raw.sfx_barcode_beep, 1)
  }

  fun playBeep() {
    if (isReleased.get() || !isLoaded.get() || beepSoundId == 0) return
    soundPool.play(beepSoundId, 1f, 1f, 1, 0, 1f)
  }

  fun release() {
    if (isReleased.compareAndSet(false, true)) {
      soundPool.release()
    }
  }
}
