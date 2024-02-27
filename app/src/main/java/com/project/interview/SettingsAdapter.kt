package com.project.interview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.interview.databinding.InterviewBsItemBinding

class SettingsAdapter: RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {
    private lateinit var interviewList: ArrayList<String>
    private lateinit var binding: InterviewBsItemBinding
    private var itemClickListener : InterviewDeleteListener? = null

    interface InterviewDeleteListener{
        fun onClick(position : Int)
    }

    fun setInterviewDeleteListener(listener : InterviewDeleteListener){
        itemClickListener = listener
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){

        fun bind(position: Int){
            binding.interviewBsTitle.text = interviewList[position]
            if(position < 3) binding.interviewBsDelete.visibility = View.GONE
        }
    }

    fun setInterviewList(list: ArrayList<String>){
        interviewList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsAdapter.ViewHolder {
        binding = InterviewBsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: SettingsAdapter.ViewHolder, position: Int) {
        holder.bind(position)
        binding.interviewBsDelete.setOnClickListener {
            interviewList.removeAt(position)
            MyApplication.prefs.setString("InterviewList", TransformJson.setArrayListToJson(interviewList))
            notifyItemRemoved(position)
            notifyItemRangeChanged(0, interviewList.size)
            itemClickListener?.onClick(position)
        }
    }

    override fun getItemCount(): Int = interviewList.size
}