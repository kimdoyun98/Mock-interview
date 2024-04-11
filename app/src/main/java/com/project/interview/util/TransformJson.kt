package com.project.interview.util

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

object TransformJson {
    fun setArrayListToJson(list: ArrayList<String>): String =
        GsonBuilder().create().toJson(list, object : TypeToken<ArrayList<String>>(){}.type)
    
    fun setJsonToArrayList(json: String): ArrayList<String> =
        GsonBuilder().create().fromJson(json, object : TypeToken<ArrayList<String>>(){}.type)
}