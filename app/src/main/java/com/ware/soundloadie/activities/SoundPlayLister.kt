package com.ware.soundloadie.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.ware.soundloadie.App
import com.ware.soundloadie.Dbase.Playlist
import com.ware.soundloadie.Dbase.PlaylistSquare
import com.ware.soundloadie.R
import com.ware.soundloadie.UniversalModel.FinderModel2
import com.ware.soundloadie.UniversalModel.PlayFinder
import com.ware.soundloadie.UniversalModel.PlayFinderModel
import io.objectbox.Box
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.chart_view_props.*
import kotlinx.android.synthetic.main.sound_play_lister.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class SoundPlayLister : AppCompatActivity() {
    var playlistSquare: Box<PlaylistSquare>? = null
    var observable:  Observable<String>? = null
    var arraygoals =  ArrayList<PlayFinderModel>()
    var uri:String? = null
    var keyID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sound_play_lister)

        keyID = getString(R.string.cloudID)
        val intents = intent.extras
        val name = intents.getString("name")
        val query = intents.getString("query")
        val image = intents.getString("image")
        toolbar.title = name
        sub_title.text = getString(R.string.playlist_count)
        titlez.text = name
        collapsingToolbar.title = name
        collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE)
        Glide.with(applicationContext).load(image).into(coverImages)
        Glide.with(applicationContext).load(image).into(blurImage)
        playlistSquare = (application as App).boxStore.boxFor(PlaylistSquare::class.java)


        val adRequest = AdRequest.Builder()
                .build()
        adView.loadAd(adRequest)


        findPlaylist(query)

    }








    fun findPlaylist(urls:String)
    {

        arraygoals = ArrayList<PlayFinderModel>()

        arraygoals.clear()
        swipes.isRefreshing = true
        recycler_view.adapter = null


        val pre = "https://api-v2.soundcloud.com/search/playlists_without_albums?q="
        val post = "&variant_ids=850&client_id=$keyID&limit=200"
        uri = pre + urls.replace(" ","%20") + post
        observable = Observable.create({ subscriber ->
            try {

                val client = OkHttpClient().newBuilder()
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS).build()
                val request = Request.Builder()
                        .url(uri)
                        .build()
                val response = client.newCall(request).execute()
                subscriber.onNext(response.body()!!.string())
                subscriber.onComplete()
                if (!response.isSuccessful)
                    subscriber.onError(Exception(response.toString()))
            } catch (e: IOException) {
                subscriber.onError(e)
            }
        })

        observable!!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: Observer<String> {
                    override fun onSubscribe(d: Disposable) {


                    }

                    override fun onComplete() {
                        swipes.isRefreshing = false;
                        val playware = PlayFinder(applicationContext,arraygoals,playlistSquare!!,constantines,this@SoundPlayLister)

                        if (playware.itemCount != 0) {
                            recycler_view.setHasFixedSize(true)
                            recycler_view.layoutManager =  LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
                            recycler_view.adapter = playware  // set adapter on recyclerview
                            playware.notifyDataSetChanged()
                            swipes.isRefreshing = false
                        } else {
                            swipes.isRefreshing = false


                        }
                    }

                    override fun onError(e: Throwable) {



                    }

                    override fun onNext(response: String) {

                        try {
                            val json = JSONObject(response)
                            val jsonarr = json.getJSONArray("collection")

                            for (i in 0..jsonarr.length() - 1) {

                                val jsonobj = jsonarr.getJSONObject(i)


                                val titles = jsonobj.getString("title")
                                val author = jsonobj.getJSONObject("user").getString("username")
                                val art    = jsonobj.getString("artwork_url")
                                val trackcount =  jsonobj.getString("track_count")
                                val link = jsonobj.getString("uri")
                                val ids = jsonobj.getString("id")
                                arraygoals.add(PlayFinderModel(titles, author, link, art, ids,trackcount))


                            }


                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }





                    }
                })

    }








    fun viewPlaylist(id:String, name:String, image:String, count:String){

        val intents = Intent(this@SoundPlayLister,PlaylistView::class.java)
        intents.putExtra("id",id)
        intents.putExtra("name",name)
        intents.putExtra("image",image)
        intents.putExtra("count",count)
        startActivity(intents)



    }


}
