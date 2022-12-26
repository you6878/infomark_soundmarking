package kr.co.infomark.soundmasking.util

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import androidx.lifecycle.MutableLiveData
import java.io.File
import java.util.*

class MusicBox :OnCompletionListener,MediaPlayer.OnPreparedListener {
    private var mPlayer: MediaPlayer? = null

    var playFiles = mutableListOf<File>()
    var currentPlayMusicName : MutableLiveData<String> = MutableLiveData("")
    var currentIndex = 0

    var isPlay : MutableLiveData<Boolean> = MutableLiveData(false)
    var randomPlay : MutableLiveData<Boolean> = MutableLiveData(false)

    var repeatPlay : MutableLiveData<Boolean> = MutableLiveData(false)

    fun playMusic(path : String) {

        if (mPlayer != null) {
            try {
                mPlayer?.stop()
                mPlayer?.reset()
//                mPlayer?.prepare()
            }  catch (e: Exception) {
                e.printStackTrace()

            }
        }else{
            //streaming music on the connected A2DP device
            mPlayer = MediaPlayer()
            mPlayer?.setOnCompletionListener(this)
            mPlayer?.setOnPreparedListener(this)
            mPlayer?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
            )
        }


        try {
            mPlayer?.setDataSource(path)
            mPlayer?.prepareAsync()
            isPlay.value = true
            currentPlayMusicName.value = File(path).name
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    fun playMusic() {
        val file = playFiles[currentIndex]
        playMusic(file.absolutePath)
        currentPlayMusicName.value = file.name

    }


    override fun onPrepared(player: MediaPlayer?) {
        player?.start()
        isPlay.value = true
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
        mPlayer?.prepare()
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