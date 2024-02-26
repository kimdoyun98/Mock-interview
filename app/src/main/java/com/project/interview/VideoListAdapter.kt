package com.project.interview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.SystemClock
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.project.interview.databinding.VideoBsLayoutBinding
import com.project.interview.databinding.VideoItemBinding
import java.io.File

class VideoListAdapter : RecyclerView.Adapter<VideoListAdapter.ViewHolder>() {
    lateinit var binding : VideoItemBinding
    lateinit var bottomSheetDialog: BottomSheetDialog

    interface InterviewOnClickListener{
        fun onClick(pos: Int)
    }

    private lateinit var videoList : ArrayList<Video>
    private lateinit var context:Context
    private var itemClickListener : InterviewOnClickListener? = null
    private var viewType = 0

    fun setInterviewOnClickListener(listener: InterviewOnClickListener){
        itemClickListener = listener
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            binding.root.setOnClickListener {
                val pos = adapterPosition
                itemClickListener?.onClick(pos)
            }
        }
        fun bind(video : Video){
            binding.thumbnail.setImageBitmap(createThumbnail(video.contentUri))
            binding.videoName.text = video.displayName
            binding.videoSize.text = android.text.format.Formatter.formatFileSize(context, video.size)
            binding.videoDuration.text = timeConversion(video.duration)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(list : ArrayList<Video>, context: Context, viewType: Int){
        videoList = list
        this.context = context
        this.viewType = viewType
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = VideoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(videoList[position])

        if(viewType == 1){
            binding.videoMenu.visibility = View.GONE
            binding.videoName.setTextColor(Color.WHITE)
            binding.videoSize.setTextColor(Color.WHITE)
        }

        binding.videoMenu.setOnClickListener {
            bottomSheetDialog = BottomSheetDialog(context, R.style.BottomSheetTheme)

            val bsBinding = VideoBsLayoutBinding.inflate(LayoutInflater.from(context))
            bsBinding.bsPlay.setOnClickListener{
                binding.root.performClick()
                bottomSheetDialog.dismiss()
            }
            bsBinding.bsRename.setOnClickListener {
                val file = File(videoList[position].path)

                val title = videoList[position].displayName.substring(0, videoList[position].displayName.lastIndexOf("."))
                val editText = EditText(context)

                editText.setText(title)
                editText.requestFocus()

                val builder = AlertDialog.Builder(context)
                builder.setTitle("파일명을 입력해주세요.")
                    .setView(editText)
                    .setPositiveButton("확인"){ _, _ ->
                        val onlyPath = file.parentFile?.absolutePath
                        var ext = file.absolutePath
                        ext = ext.substring(ext.lastIndexOf("."))
                        val newPath = "${onlyPath}/${editText.text}${ext}"

                        val newFile = File(newPath)
                        val rename = file.renameTo(newFile)
                        if(rename){
                            val resolver = context.applicationContext.contentResolver
                            resolver.delete(MediaStore.Files.getContentUri("external"),
                                MediaStore.MediaColumns.DATA+"=?",
                                arrayOf(file.absolutePath)
                            )

                            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                            intent.setData(Uri.fromFile(newFile))
                            context.applicationContext.sendBroadcast(intent)

                            notifyItemRangeChanged(position, videoList.size)
                            Toast.makeText(context, "성공", Toast.LENGTH_LONG).show()

                            SystemClock.sleep(100)
                            (context as Activity).recreate()
                        }
                        else Toast.makeText(context, "실패", Toast.LENGTH_LONG).show()

                    }
                    .setNegativeButton("취소") { dialog, _ ->
                        dialog?.dismiss()
                    }
                    .create()
                    .show()
                bottomSheetDialog.dismiss()

            }

            bsBinding.bsDelete.setOnClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("삭제")
                    .setMessage("삭제하시겠습니까?")
                    .setPositiveButton("예"){ _, _ ->
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            videoList[position].id)
                        val file = File(videoList[position].path)
                        val delete = file.delete()
                        if(delete){
                            Toast.makeText(context, "${videoList[position].displayName} 삭제 완료", Toast.LENGTH_LONG).show()
                            context.contentResolver.delete(contentUri, null, null)
                            videoList.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, videoList.size)
                        }
                        else Toast.makeText(context, "에러", Toast.LENGTH_LONG).show()
                    }
                    .setNegativeButton("아니오"){ dialog , _ ->
                        dialog?.dismiss()
                    }
                    .create()
                    .show()

                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.setContentView(bsBinding.root)
            bottomSheetDialog.show()
        }
    }

    override fun getItemCount(): Int = videoList.size

    private fun createThumbnail(uri: Uri): Bitmap?{
        var mediaMetadataRetriever: MediaMetadataRetriever? = null
        var bitMap : Bitmap? = null

        try {
            mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(context, uri)
            bitMap = mediaMetadataRetriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        }
        catch (e:Exception){
            e.printStackTrace()
        }
        finally {
            if(mediaMetadataRetriever != null){
                mediaMetadataRetriever.release()
            }
        }

        return bitMap
    }

    private fun timeConversion(values: Long): String{
        val duration = values.toInt()
        val hrs = duration/3600000
        val mns = (duration/6000) % 60000
        val scs = duration%60000/1000

        return if(hrs > 0) String.format("%02d:%02d:%02d", hrs, mns, scs) else String.format("%02d:%02d", mns, scs)
    }
}