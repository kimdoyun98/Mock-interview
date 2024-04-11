package com.project.interview.ui.videolist

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.project.interview.R
import com.project.interview.data.Video
import com.project.interview.adapter.VideoListAdapter
import com.project.interview.databinding.ActivityInterviewVideoListBinding
import com.project.interview.ui.recording.RecordingInterview
import com.project.interview.ui.video.Exoplayer

class InterviewVideoList : AppCompatActivity() {
    private lateinit var binding : ActivityInterviewVideoListBinding
    private lateinit var name : String
    private val videoList = ArrayList<Video>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInterviewVideoListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        name = intent.getStringExtra("name")!!
        supportActionBar?.title = name

        getVideoList(this)

        val adapter = VideoListAdapter().apply {
            setList(videoList, this@InterviewVideoList, 0)
        }
        binding.videoList.adapter = adapter

        adapter.setInterviewOnClickListener(object : VideoListAdapter.InterviewOnClickListener {
            override fun onClick(pos: Int) {
                val intent = Intent(this@InterviewVideoList, Exoplayer::class.java)
                intent.putExtra("videoList", videoList)
                intent.putExtra("position", pos)
                intent.putExtra("folder", name)
                startActivity(intent)
            }
        })
    }

    private fun getVideoList(context: Context){
        val selection = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.RELATIVE_PATH + " like ? "
        } else null
        val selectionArgs = arrayOf("%$name%")
        val sortOrder = MediaStore.MediaColumns.DATE_ADDED + " COLLATE NOCASE DESC"

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            null,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                val displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))
                val contentUri = Uri.withAppendedPath(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )
                val mineType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE))
                val duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))
                val size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)) // byte
                val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                videoList.add(
                    Video(
                        id, displayName, contentUri, mineType, duration, size, path
                    )
                )
            }
            cursor.close()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
            R.id.add_video -> {
                val intent = Intent(this, RecordingInterview::class.java)
                intent.putExtra("name", name)
                startActivity(intent)
            }
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.videolist_toolbar_menu, menu)
        return true
    }

    private var status = true
    override fun onResume() {
        super.onResume()
        if(!status) recreate()
        status = true
    }

    override fun onPause() {
        super.onPause()
        status = false
    }
}