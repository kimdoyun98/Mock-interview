package com.project.interview

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.project.interview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    private val interviewList = arrayOf("1분 자기소개", "성격 장단점", "지원동기")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * Youtube
         */
        val youtubeAdapter = YoutubeAdapter()
        binding.tutorialVideo.adapter = youtubeAdapter

        /**
         * 개별 연습
         */
        val adapter = InterviewListAdapter().apply {
            setList(interviewList)
        }
        binding.interviewList.adapter = adapter


        adapter.setInterviewOnClickListener(object : InterviewListAdapter.InterviewOnClickListener {
            override fun onClick(string: String) {
                val intent = Intent(this@MainActivity, InterviewVideoList::class.java)
                intent.putExtra("name", string)
                startActivity(intent)
            }
        })

        /**
         * firebase Test
         */

    }
}