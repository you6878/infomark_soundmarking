package kr.co.infomark.soundmasking.util

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import androidx.lifecycle.MutableLiveData
import java.io.File
import java.io.IOException
import java.util.*

class MusicBox :OnCompletionListener {
    private var mPlayer: MediaPlayer? = null

    var playFiles = mutableListOf<File>()
    var currentPlayMusicName : MutableLiveData<String> = MutableLiveData("")
    var currentIndex = -1

    var isPlay : MutableLiveData<Boolean> = MutableLiveData(false)
    var randomPlay : MutableLiveData<Boolean> = MutableLiveData(false)

    var repeatPlay : MutableLiveData<Boolean> = MutableLiveData(false)

    fun playMusic(path : String) {

        if (mPlayer != null) {
            try {
                mPlayer?.stop()
                mPlayer?.prepare()
            }  catch (e: Exception) {
                e.printStackTrace()
            }
        }

        //streaming music on the connected A2DP device
        mPlayer = MediaPlayer()
        mPlayer?.setOnCompletionListener(this)
        try {
            mPlayer?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
            )
            mPlayer?.setDataSource(path)
            mPlayer?.prepare()
            mPlayer?.start()
            isPlay.value = true
            currentPlayMusicName.value = File(path).name
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    fun playMusic() {
        if(playFiles.size == 0){
            return
        }
        val file = playFiles[currentIndex]
        if (mPlayer != null) {
            try {
                mPlayer?.stop()
                mPlayer?.prepare()
            }  catch (e: IOException) {
                e.printStackTrace()
            }
        }

        //streaming music on the connected A2DP device
        mPlayer = MediaPlayer()
        mPlayer?.setOnCompletionListener(this)
        try {
            mPlayer?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
            )
            mPlayer?.setDataSource(file.absolutePath)
            mPlayer?.prepare()
            mPlayer?.start()
            isPlay.value = true
            currentPlayMusicName.value = file.name
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
    fun releaseMediaPlayer() {
        mPlayer?.release()
    }
    fun getMPlayerCurrentTime() : Int{
        return mPlayer?.currentPosition ?: 0
    }
    fun getMPlayerDurationTime() : Int{
        return mPlayer?.duration ?: 0
    }
    fun getMPlaying(): Boolean {
        return mPlayer?.isPlaying ?: false
    }
    fun stopMusic(){
        mPlayer?.stop()
        isPlay.value = false
    }

    fun setPlayList(lists : MutableList<File>){
        playFiles = lists
    }
    fun nextMusic(){
        if(playFiles.size == 0){
            return
        }
        val maxSize = playFiles.size - 1
        if(randomPlay.value == true){
            currentIndex = makeRandom(maxSize)
        }else if(currentIndex < maxSize){
            currentIndex ++
        }else{
            currentIndex = 0
        }
        val file = playFiles[currentIndex]
        currentPlayMusicName.value = file.name
        playMusic(file.absolutePath)
    }
    fun previewMusic(){
        if(playFiles.size == 0){
            return
        }
        val maxSize = playFiles.size - 1
        if(randomPlay.value == true){
            currentIndex = makeRandom(maxSize)
        } else if(currentIndex > 0){
            currentIndex --
        }else{
            currentIndex = maxSize
        }
        val file = playFiles[currentIndex]
        currentPlayMusicName.value = file.name
        playMusic(file.absolutePath)
    }
    fun eventRandomBtn(){
        if(randomPlay.value == false){
            repeatPlay.value = false
            randomPlay.value = true
        }else{
            randomPlay.value = false
        }

    }
    fun eventRepeatBtn(){

        if(repeatPlay.value == false) {
            repeatPlay.value = true
            randomPlay.value = false
        }else{
            repeatPlay.value = false
        }
    }

    fun makeRandom( range : Int) : Int{
        val value = Random().nextInt(range)
            if(currentIndex == value){
                makeRandom(range)
            }
        return value
    }

    override fun onCompletion(p0: MediaPlayer?) {
        if(repeatPlay.value == true){
            playMusic()
        }else{
            nextMusic()
        }
    }
}