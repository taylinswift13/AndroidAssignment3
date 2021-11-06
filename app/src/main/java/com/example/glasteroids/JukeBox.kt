package com.example.glasteroids

import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import java.io.IOException

object SFX{
    var asteroid_crash = 0
    var start = 0
    var player_hurt =0
    var bullet = 0
    var lose =0
    var win = 0
}
const val MAX_STREAMS = 3
var isFirstInitialize = true

class Jukebox(private val assetManager: AssetManager) {

    private val soundPool: SoundPool
    init {
        val attr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setAudioAttributes(attr)
            .setMaxStreams(MAX_STREAMS)
            .build()

        soundPool.setOnLoadCompleteListener { _, _, _ ->
            if(isFirstInitialize){
                play(SFX.start)
                isFirstInitialize = false
            }
        }
        SFX.asteroid_crash = loadSound("asteroid_crash.wav")
        SFX.player_hurt = loadSound("player_hurt.wav")
        SFX.start = loadSound("start.wav")
        SFX.bullet = loadSound("bullet.wav")
        SFX.lose = loadSound("lose.wav")
        SFX.win = loadSound("win.wav")
    }

    private fun loadSound(fileName: String): Int{
        try {
            val descriptor: AssetFileDescriptor = assetManager.openFd(fileName)
            return soundPool.load(descriptor, 1)
        }catch(e: IOException){
            Log.d(TAG, "Unable to load $fileName! Check the filename, and make sure it's in the assets-folder.")
        }
        return 0
    }

    fun play(soundID: Int) {
        val leftVolume = 1f
        val rightVolume = 1f
        val priority = 0
        val loop = 0
        val playbackRate = 1.0f
        if (soundID > 0) {
            soundPool.play(soundID, leftVolume, rightVolume, priority, loop, playbackRate)
        }
    }

    companion object{
        const val TAG = "Jukebox"
    }

}