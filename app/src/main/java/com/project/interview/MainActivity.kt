package com.project.interview

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.project.interview.databinding.ActivityMainBinding
import com.project.interview.databinding.InterviewBsLayoutBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var interviewList: ArrayList<String>

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
        interviewList = TransformJson.setJsonToArrayList(MyApplication.prefs.getString("InterviewList"))
        Log.e("MainActivity", interviewList.toString())
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
         * Settings Button Click -> BottomSheetDialog
         */
        binding.settings.setOnClickListener {
            bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetTheme)
            val settingBinding = InterviewBsLayoutBinding.inflate(LayoutInflater.from(this))
            val settingsAdapter = SettingsAdapter()

            /**
             * 추가 Button Click -> AlertDialog
             */
            settingBinding.interviewBsAdd.setOnClickListener {
                val editText = EditText(this)
                editText.requestFocus()

                val builder = AlertDialog.Builder(this)
                builder.setTitle("면접 질문을 입력해주세요.")
                    .setView(editText)
                    .setPositiveButton("확인"){ _, _ ->
                        interviewList.add("${editText.text}")
                        MyApplication.prefs.setString("InterviewList", TransformJson.setArrayListToJson(interviewList))
                        settingsAdapter.setInterviewList(interviewList)
                    }
                    .setNegativeButton("취소") { dialog, _ ->
                        dialog?.dismiss()
                    }
                    .create()
                    .show()
            }

            /**
             * Adapter Set
             */
            settingBinding.interviewRv.adapter = settingsAdapter.apply {
                setInterviewList(interviewList)
            }

            /**
             * Main Adapter 갱신
             */
            settingsAdapter.setInterviewDeleteListener(object : SettingsAdapter.InterviewDeleteListener{
                override fun onClick(position: Int) {
                    adapter.setList(interviewList)
                }
            })

            bottomSheetDialog.setContentView(settingBinding.root)
            bottomSheetDialog.show()
        }
    }
}