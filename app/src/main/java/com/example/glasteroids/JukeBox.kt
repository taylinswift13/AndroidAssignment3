package com.example.glasteroids

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import java.io.IOException

private const val MAX_STREAMS = 6

enum class GameEvent {
    Asteroid_crash, PlayerHurt, Bullut, GameOver, Win,Start
}

private const val SOUNDS_PREF_KEY = "sounds_pref_key"
private const val MUSIC_PREF_KEY = "music_pref_key"

class Jukebox(val engine: Game) {
    private val DEFAULT_SFX_VOLUME = 0.7f
    val TAG = "Jukebox"
    private var mSoundPool: SoundPool? = null
    private var mBgPlayer: MediaPlayer? = null
    private val mSoundsMap = HashMap<GameEvent, Int>()
    private var mSoundEnabled: Boolean = false
    private var mMusicEnabled: Boolean = false

    init {
        engine.getActivity().volumeControlStream = AudioManager.STREAM_MUSIC;
        val prefs = engine.getPreferences()
        if (prefs != null) {
            mSoundEnabled = prefs.getBoolean(SOUNDS_PREF_KEY, true)
        }
        if (prefs != null) {
            mMusicEnabled = prefs.getBoolean(MUSIC_PREF_KEY, true)
        }
        loadIfNeeded()
    }

    private fun loadIfNeeded() {
        if (mSoundEnabled) {
            loadSounds()
        }
        if (mMusicEnabled) {
            loadMusic()
        }
    }

    private fun loadSounds() {
        val attr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        mSoundPool = SoundPool.Builder()
            .setAudioAttributes(attr)
            .setMaxStreams(MAX_STREAMS)
            .build()

        mSoundsMap.clear()
        loadEventSound(GameEvent.Asteroid_crash, "sfx/asteroid_crash.wav")
        loadEventSound(GameEvent.PlayerHurt, "sfx/player_hurt.wav")
        loadEventSound(GameEvent.Bullut, "sfx/bullet.wav")
        loadEventSound(GameEvent.GameOver, "sfx/lose.wav")
        loadEventSound(GameEvent.Win, "sfx/win.wav")
        loadEventSound(GameEvent.Start, "sfx/start.wav")
    }
    private fun unloadSounds() {
        if (mSoundPool == null) {
            return
        }
        mSoundPool!!.release()
        mSoundPool = null
        mSoundsMap.clear()
    }
    private fun loadEventSound(event: GameEvent, fileName: String) {
        try {
            val afd = engine.getAssets().openFd(fileName)
            val soundId = mSoundPool!!.load(afd, 1)
            mSoundsMap[event] = soundId
        } catch (e: IOException) {
            Log.e(TAG, "Error loading sound $e")
        }
    }
    fun playEventSound(event: GameEvent) {
        if (!mSoundEnabled) {
            return
        }
        val leftVolume = DEFAULT_SFX_VOLUME
        val rightVolume = DEFAULT_SFX_VOLUME
        val priority = 1
        val loop = 0 //-1 loop forever, 0 play once
        val rate = 1.0f
        val soundID = mSoundsMap[event]
        if(soundID == null){
            Log.e(TAG, "Attempting to play non-existent event sound: {event}")
            return
        }
        if (soundID > 0) { //if soundID is 0, the file failed to load. Make sure you catch this in the loading routine.
            mSoundPool!!.play(soundID, leftVolume, rightVolume, priority, loop, rate)
        }
    }
    private fun loadMusic() {
        try {
            mBgPlayer = MediaPlayer()
            val afd = engine.getAssets().openFd("bgm/bgm.mp3")
            mBgPlayer!!.setDataSource(
                afd.fileDescriptor,
                afd.startOffset,
                afd.length
            )
            mBgPlayer!!.isLooping = true
            mBgPlayer!!.setVolume(DEFAULT_SFX_VOLUME,DEFAULT_SFX_VOLUME)
            mBgPlayer!!.prepare()
        } catch (e: IOException) {
            Log.e(TAG, "Unable to create MediaPlayer.", e)
        }
    }
    private fun unloadMusic() {
        if (mBgPlayer == null) {
            return
        }
        mBgPlayer!!.stop()
        mBgPlayer!!.release()
    }
    fun pauseBgMusic() {
        if (!mMusicEnabled) {
            return
        }
        mBgPlayer!!.pause()
    }

    fun resumeBgMusic() {
        if (!mMusicEnabled) {
            return
        }
        mBgPlayer!!.start()
    }
    fun toggleSoundStatus() {
        mSoundEnabled = !mSoundEnabled
        if (mSoundEnabled) {
            loadSounds()
        } else {
            unloadSounds()
        }
        engine.savePreference(SOUNDS_PREF_KEY, mSoundEnabled)
    }
    fun toggleMusicStatus() {
        mMusicEnabled = !mMusicEnabled
        if (mMusicEnabled) {
            loadMusic()
        } else {
            unloadMusic()
        }
        engine.savePreference(MUSIC_PREF_KEY, mSoundEnabled)
    }


}