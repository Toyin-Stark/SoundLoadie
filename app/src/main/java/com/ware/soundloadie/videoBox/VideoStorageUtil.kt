package com.ware.soundloadie.videoBox

import android.content.Context
import android.content.SharedPreferences

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import java.util.ArrayList

class VideoStorageUtil(private val context: Context) {

    private val STORAGE = " com.naira.soundloadd.VIDEOSTORAGE"
    private var preferences: SharedPreferences? = null

    fun storeVideo(arrayList: ArrayList<Video>) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)

        val editor = preferences!!.edit()
        val gson = Gson()
        val json = gson.toJson(arrayList)
        editor.putString("videoArrayList", json)
        editor.apply()
    }

    fun loadVideo(): ArrayList<Video> {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = preferences!!.getString("videoArrayList", null)
        val type = object : TypeToken<ArrayList<Video>>() {

        }.type
        return gson.fromJson<ArrayList<Video>>(json, type)
    }

    fun storeVideoIndex(index: Int) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences!!.edit()
        editor.putInt("videoIndex", index)
        editor.apply()
    }

    fun loadVideoIndex(): Int {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        return preferences!!.getInt("videoIndex", -1)//return -1 if no data found
    }

    fun clearCachedVideoPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences!!.edit()
        editor.clear()
        editor.commit()
    }
}
