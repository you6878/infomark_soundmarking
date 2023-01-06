package kr.co.infomark.soundmasking.util

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.view.View
import androidx.lifecycle.MutableLiveData
import kr.co.infomark.soundmasking.MainActivity
import java.io.File
import java.io.FileNotFoundException

class MusicBox(var mainActivity : MainActivity) :OnCompletionListener,MediaPlayer.OnPreparedListener {


    private var mPlayer: MediaPlayer? = null

    var playFilesOrigin = mutableListOf<File>()
    var playFiles = mutableListOf<File>()
    var currentPlayMusicName : MutableLiveData<String> = MutableLiveData("")
    var currentIndex = 0
    var selectFileSize = -1L

    var isPlay : MutableLiveData<Boolean> = MutableLiveData(false)
    var randomPlay : MutableLiveData<Boolean> = MutableLiveData(false)

    var repeatPlay : MutableLiveData<Int> = MutableLiveData(0)

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
            var file = File(path)
            mPlayer?.setDataSource(path)
            mPlayer?.prepareAsync()
            isPlay.value = true
            selectFileSize = file.length()
            currentPlayMusicName.value = file.name
        } catch (e: FileNotFoundException) {
            mainActivity.loadFile()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    fun checkPlayFileByPosition(){

    }
    fun playMusic() {
        if(playFiles.size == 0){
            return
        }
        playFiles.forEachIndexed { index, file ->
            if(file.length() == selectFileSize){
                playMusic(file.absolutePath)
                currentIndex = index
                currentPlayMusicName.value = file.name
            }else{
                val file = playFiles[0]
                playMusic(file.absolutePath)
                currentPlayMusicName.value = file.name
            }
        }



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
        isPlay.value = false
    }

    fun setPlayList(lists : List<File>){
        if(lists.size == 0)
            return

        playFilesOrigin = lists.toMutableList()
        playFiles = lists.toMutableList()

        if (randomPlay.value == true){
            setRandomList()
        }
    }

    fun setRandomList(){
        if(playFiles.size == 0)
            return

        playFiles.shuffle()
        //Remove CurrentMusic
        var firstFile : File? = null
        var removeIndex = 0
        playFiles.forEachIndexed { index, file ->
            if(file.length() == selectFileSize){
                removeIndex = index
                firstFile = file
            }
        }
        playFiles.removeAt(removeIndex)
        firstFile?.let { playFiles.add(0, it) }
        currentIndex = 0

    }
    fun setOriginList(){
        playFiles = playFilesOrigin.toMutableList()
    }
    fun nextMusic(){
        if(playFiles.size == 0){
            return
        }
        val maxSize = playFiles.size - 1
//        if(randomPlay.value == true){
//            currentIndex = makeRandom(maxSize)
//        }else
            if(currentIndex < maxSize){
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
//        if(randomPlay.value == true){
//            currentIndex = makeRandom(maxSize)
//        } else
            if(currentIndex > 0){
            currentIndex --
        }else{
            currentIndex = maxSize
        }
        val file = playFiles[currentIndex]
        currentPlayMusicName.value = file.name
        playMusic(file.absolutePath)
    }
    fun eventRandomBtn(view: View) {
        if(randomPlay.value == false){
            randomPlay.value = true
            setRandomList()
            if(repeatPlay.value == 1) {
                repeatPlay.value = 0
            }

        }else{
            randomPlay.value = false
            setOriginList ()
        }



    }
    fun eventRepeatBtn(){

        if(repeatPlay.value == 0) {
            repeatPlay.value = 1
            randomPlay.value = false

        }else if(repeatPlay.value == 1) {
            repeatPlay.value = 2
        } else{
            repeatPlay.value = 0
        }
    }
//
//    fun makeRandom( range : Int) : Int{
//        val value = Random().nextInt(range)
//            if(currentIndex == value){
//                makeRandom(range)
//            }
//        return value
//    }

    override fun onCompletion(p0: MediaPlayer?) {
        if(repeatPlay.value == 1){
            //한곡
            playMusic()
        }else if(repeatPlay.value == 2){
            //여러곡반복
            nextMusic()
        } else {
//            //랜덤
//            if(randomPlay.value == true){
//
//            }

            println("currentIndex : ${currentIndex}")

            //마지막 정지
            if(currentIndex < playFiles.size - 1){
                nextMusic()
            }else{
                isPlay.value = false
            }
        }
    }
}