package com.project.interview.data

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Video(
    val id: Long,
    val displayName: String,
    val contentUri: Uri,
    val mineType: String,
    val duration: Long,
    val size: Long,
    val path: String
):Parcelable
