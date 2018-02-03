package com.ware.soundloadie.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.widget.Toast
import com.ware.soundloadie.App
import com.ware.soundloadie.Dbase.VPlaylist
import com.ware.soundloadie.R
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
import kotlinx.android.synthetic.main.video_finder.*
import okhttp3.OkHttpClient
import okhttp3.Request.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class VideoFinder : AppCompatActivity() {
    var observable: Observable<String>? = null
    var videoList  =  ArrayList<Video>()
    var videoplaylist: Box<VPlaylist>? = null
    var bunkerString = ""
    var wave: VideoPlugAdapter? = null
    var list:ArrayList<VideoModel>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_finder)

        videoplaylist = (application as App).boxStore.boxFor(VPlaylist::class.java)

        findwidget.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {


                return false
            }

            override fun onQueryTextSubmit(query: String?): Boolean {

                bunkerString = query!!
               findVideo(bunkerString)
                return false
            }


        })


    }
















    //======================================================Search Videos=========================================================//


    fun findVideo(query:String)
    {
        swipes.isRefreshing = true
        list = ArrayList<VideoModel>()
        list!!.clear()
        videoList.clear()

        var body = ""



        recyclerview.adapter = null
        recyclerview.isNestedScrollingEnabled = true
        recyclerview.layoutManager =  LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)

        if (observable !=null){

            observable!!.unsubscribeOn(Schedulers.io())
        }

        observable = Observable.create(object: ObservableOnSubscribe<String> {
            override fun subscribe(subscriber: ObservableEmitter<String>) {


                try {

                    val client = OkHttpClient().newBuilder()
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS).build()
                    val request = Builder()
                            .url("https://www.googleapis.com/youtube/v3/search?part=id,snippet&q=$query&type=video&maxResults=50&key=AIzaSyAIlhBQLr9qBk9UOJHvU-E5rbxCwvtEOZ4")
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
                        val id =   jsonobj.getJSONObject("id").getString("videoId")


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

                            recyclerview.adapter = wave// set adapter on recyclerview
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
