package com.project.interview

import android.annotation.SuppressLint
import android.graphics.Point
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.project.interview.databinding.ActivityExoplayerBinding
import com.project.interview.databinding.CustomPlaybackViewBinding

class Exoplayer : AppCompatActivity(), View.OnClickListener{
    private lateinit var binding : ActivityExoplayerBinding
    private lateinit var exoplayerBinding: CustomPlaybackViewBinding

    private lateinit var videoList: ArrayList<Video>
    private var position = -1
    private lateinit var player: SimpleExoPlayer
    private lateinit var concatenatingMediaSorce : ConcatenatingMediaSource

    private var waitTimeLeft = 0L
    private var waitTimeRight = 0L
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExoplayerBinding.inflate(layoutInflater)
        exoplayerBinding = CustomPlaybackViewBinding.bind(binding.root)
        setFullScreen()
        setContentView(binding.root)

        videoList = intent.getParcelableArrayListExtra("videoList")!!
        position = intent.getIntExtra("position", -1)

        exoplayerBinding.videoTitle.text = videoList[position].displayName

        exoplayerBinding.exoNext.setOnClickListener(this)
        exoplayerBinding.exoPrev.setOnClickListener(this)
        exoplayerBinding.videoBack.setOnClickListener(this)
        exoplayerBinding.videoList.setOnClickListener(this)

        playVideo()

        /**
         * 더블 클릭 시 10초 앞뒤로 이동
         */
        binding.exoplayerView.setOnTouchListener { _, event ->
            if(event.actionMasked == MotionEvent.ACTION_DOWN){
                val size = Point()
                windowManager.defaultDisplay.getSize(size)

                val x = event.x // 터치 좌표

                // Right
                if(size.x/2 < x){
                    // 한 번 터치
                    if(System.currentTimeMillis() - waitTimeRight >=500 ) {
                        waitTimeRight = System.currentTimeMillis()
                        waitTimeLeft = 0L
                    }
                    // 두 번 터치 시 10초 앞으로
                    else {
                        player.seekTo(player.currentPosition + 10000)
                    }
                }
                //Left
                else{
                    if(System.currentTimeMillis() - waitTimeLeft >=500 ) {
                        waitTimeLeft = System.currentTimeMillis()
                        waitTimeRight = 0L
                    }
                    // 두 번 터치 시 10초 뒤로
                    else {
                        player.seekTo(player.currentPosition - 10000)
                    }
                }
            }
            true
        }
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.exo_next ->{
                try{
                    player.stop()
                    position++
                    playVideo()
                }
                catch (e: Exception){
                    Toast.makeText(this, "다음 영상이 없습니다.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
            R.id.exo_prev -> {
                try{
                    player.stop()
                    position--
                    playVideo()
                }
                catch (e: Exception){
                    Toast.makeText(this, "이전 영상이 없습니다.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
            R.id.video_list -> {
                val videoListDialog = VideoListDialog()
                videoListDialog.setList(videoList, intent.getStringExtra("folder")!!)
                videoListDialog.show(supportFragmentManager, videoListDialog.tag)
            }
            R.id.video_back -> finish()
        }
    }

    private fun playVideo(){
        exoplayerBinding.videoTitle.text = videoList[position].displayName

        player = SimpleExoPlayer.Builder(this).build()
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "app"))
        concatenatingMediaSorce = ConcatenatingMediaSource()
        for(i in videoList){
            val mediaSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(i.contentUri))
            concatenatingMediaSorce.addMediaSource(mediaSource)
        }

        binding.exoplayerView.player = player
        binding.exoplayerView.keepScreenOn = true
        player.prepare(concatenatingMediaSorce)
        player.seekTo(position, C.TIME_UNSET)

        playError()
    }

    private fun playError(){
        player.addListener(object : Player.Listener{
            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(this@Exoplayer, "Video Player Error", Toast.LENGTH_LONG).show()
            }
        })
        player.playWhenReady = true
    }

    private fun setFullScreen(){
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(player.isPlaying) player.stop()
    }

    override fun onPause() {
        super.onPause()
        player.playWhenReady = false
        player.playbackState
    }

    override fun onResume() {
        super.onResume()
        player.playWhenReady = true
        player.playbackState
    }

    override fun onRestart() {
        super.onRestart()
        player.playWhenReady = true
        player.playbackState
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}