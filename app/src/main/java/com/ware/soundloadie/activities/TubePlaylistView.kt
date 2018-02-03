package com.ware.soundloadie.activities

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.bumptech.glide.Glide
import com.ware.soundloadie.Dbase.VPlaylist
import com.ware.soundloadie.UniversalModel.VideoModel
import com.ware.soundloadie.UniversalModel.VideoPlugAdapter
import com.ware.soundloadie.videoBox.Video
import io.objectbox.Box
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.chart_view_props.*
import kotlinx.android.synthetic.main.tube_playlist_view.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class TubePlaylistView : AppCompatActivity() {
    var videoList  =  ArrayList<Video>()
    var videoplaylist: Box<VPlaylist>? = null
    var observable: Observable<String>? = null
    var wave: VideoPlugAdapter? = null
    var list:ArrayList<VideoModel>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intents = intent.extras
        val name = intents.getString("name")
        val ids  = intents.getString("lock")
        val types = intents.getString("type")
        val image = intents.getString("image")

        toolbar.title = name
        sub_title.text = name
        collapsingToolbar.title = name
        collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE)

        Glide.with(applicationContext).load(image).into(coverImages)
        Glide.with(applicationContext).load(image).into(blurImage)
        startVideo(ids)
    }








    fun startVideo(playlist:String)
    {
        swipes.isRefreshing = true

        list = ArrayList<VideoModel>()
        list!!.clear()
        videoList.clear()

        var body = ""

        recycler_view.adapter = null
        recycler_view.isNestedScrollingEnabled = true
        recycler_view.layoutManager =  LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)


        observable = Observable.create(object: ObservableOnSubscribe<String> {
            override fun subscribe(subscriber: ObservableEmitter<String>) {


                try {

                    val client = OkHttpClient().newBuilder()
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS).build()
                    val request = Request.Builder()
                            .url("https://www.googleapis.com/youtube/v3/playlistItems?part=id%2Csnippet%2CcontentDetails&maxResults=50&playlistId=$playlist&key=AIzaSyAIlhBQLr9qBk9UOJHvU-E5rbxCwvtEOZ4")
                            .build()
                    val response = client.newCall(request).execute()
                    body = response.body()!!.string()
                    if (!response.isSuccessful)
                        subscriber.onError(Exception(response.toString()))
                } catch (e: IOException) {
                    subscriber.onError(e)
                }



                try {

                    val json = JSONObject(body)

                    val jsonarr = json.getJSONArray("items")

                    for (i in 0 until jsonarr.length()) {

                        val jsonobj = jsonarr.getJSONObject(i)
                        val title = jsonobj.getJSONObject("snippet").getString("title")
                        val time = jsonobj.getJSONObject("snippet").getString("publishedAt")
                        val desc = jsonobj.getJSONObject("snippet").getString("channelTitle")
                        val id = jsonobj.getJSONObject("contentDetails").getString("videoId")


                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                        val date = sdf.parse(time)
                        val startDate = date.getTime()
                        val videoUrl = "https://www.youtube.com/watch?v=" + id



                        list!!.add(VideoModel(title, videoUrl, id,startDate))
                        videoList.add(Video(title,videoUrl,id,startDate))


                    }


                    subscriber.onComplete()


                } catch (e: Throwable) {
                    e.printStackTrace()
                }


            }
        })

        observable!!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: Observer<String> {
                    override fun onSubscribe(d: Disposable) {


                    }

                    override fun onComplete() {


                        wave = VideoPlugAdapter(applicationContext,list!!)

                        if (wave!!.itemCount != 0) {

                            recycler_view.adapter = wave// set adapter on recyclerview
                            wave!!.notifyDataSetChanged()

                            swipes.isRefreshing = false

                        } else {
                            swipes.isRefreshing = false


                        }


                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(applicationContext,""+e.message,Toast.LENGTH_LONG).show()
                        swipes.isRefreshing = false

                    }

                    override fun onNext(response: String) {



                    }
                })

    }


}
