package com.project.interview

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.project.interview.databinding.VideolistBsLayoutBinding

class VideoListDialog : BottomSheetDialogFragment() {
    private lateinit var binding: VideolistBsLayoutBinding
    private lateinit var arrayList : ArrayList<Video>
    private lateinit var adapter: VideoListAdapter
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var title: String

    fun setList (arrayList: ArrayList<Video>, title: String){
        this.arrayList = arrayList
        this.title = title
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        binding = VideolistBsLayoutBinding.inflate(LayoutInflater.from(context))
        bottomSheetDialog.setContentView(binding.root)

        adapter = VideoListAdapter()
        adapter.setList(arrayList, requireContext(), 1)

        binding.videoListRv.adapter = adapter
        adapter.notifyDataSetChanged()

        adapter.setInterviewOnClickListener(object : VideoListAdapter.InterviewOnClickListener {
            override fun onClick(pos: Int) {
                val intent = Intent(context, Exoplayer::class.java)
                intent.putExtra("videoList", arrayList)
                intent.putExtra("position", pos)
                intent.putExtra("folder", title)
                startActivity(intent)
                activity?.finish()
            }
        })

        binding.videoListName.text = title

        return bottomSheetDialog
    }

}