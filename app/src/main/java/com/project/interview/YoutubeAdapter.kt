package com.project.interview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.project.interview.databinding.YoutubeItemBinding

class YoutubeAdapter : RecyclerView.Adapter<YoutubeAdapter.ViewHolder>(){
    lateinit var binding : YoutubeItemBinding
    private val youtubeLink = arrayOf("mWjlHfFwptg", "rwq7kgAGlw0")

    inner class ViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        fun bind(link : String){
            binding.youtube.addYouTubePlayerListener(object : AbstractYouTubePlayerListener(){
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    youTubePlayer.cueVideo(link, 0F)
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = YoutubeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(youtubeLink[position])
    }

    override fun getItemCount(): Int = youtubeLink.size
}