package com.project.interview

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.interview.databinding.InterviewItemBinding

class InterviewListAdapter : RecyclerView.Adapter<InterviewListAdapter.ViewHolder>() {
    interface InterviewOnClickListener{
        fun onClick(string : String)
    }
    lateinit var binding : InterviewItemBinding
    private lateinit var list : ArrayList<String>
    private var itemClickListener : InterviewOnClickListener? = null

    fun setInterviewOnClickListener(listener : InterviewOnClickListener){
        itemClickListener = listener
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            binding.root.setOnClickListener {
                val pos = adapterPosition
                itemClickListener?.onClick(list[pos])
            }
        }
        fun bind(name : String){
            binding.interviewName.text = name
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(list : ArrayList<String>){
        this.list = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = InterviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

}