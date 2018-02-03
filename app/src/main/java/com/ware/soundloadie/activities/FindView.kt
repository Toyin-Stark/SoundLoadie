package com.ware.soundloadie.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.MenuItemCompat
import android.support.v4.view.MenuItemCompat.expandActionView
import com.ware.soundloadie.R
import kotlinx.android.synthetic.main.find_view.*
import android.support.v4.view.MenuItemCompat.getActionView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.View
import com.bumptech.glide.Glide
import com.ware.soundloadie.App
import com.ware.soundloadie.AudioBox.Audio
import com.ware.soundloadie.AudioBox.Player
import com.ware.soundloadie.AudioBox.StorageUtil
import com.ware.soundloadie.Dbase.Playlist
import com.ware.soundloadie.Dbase.PlaylistSquare
import com.ware.soundloadie.UniversalModel.*
import com.ware.soundloadie.Utils.next
import com.ware.soundloadie.Utils.previous
import io.objectbox.Box
import io.objectbox.query.Query
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.badge.*
import kotlinx.android.synthetic.main.controls.*
import lib.kingja.switchbutton.SwitchMultiButton
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit


class FindView : AppCompatActivity() {
    var observable:  Observable<String>? = null
    var arraylistx =  ArrayList<FinderModel2>()
    var arrayplay =  ArrayList<PlayFinderModel>()

    var uri:String? = null
    var playlist: Box<Playlist>? = null
    var playlistSquare: Box<PlaylistSquare>? = null
    var playlistQuery: Query<Playlist>? = null
    var keyID = ""
    var audioList  =  ArrayList<Audio>()

    var sounding = false
    var audioIndex = -1
    var activeAudio: Audio? = null //an object on the currently playing audio
    var storage : StorageUtil? = null
    var bunkerString = ""
    var isPlaylist = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.find_view)

        setSupportActionBar(toolbarx)
        toolbarx.title = ""

        keyID = getString(R.string.cloudID)
        playlist = (application as App).boxStore.boxFor(Playlist::class.java)
        playlistSquare = (application as App).boxStore.boxFor(PlaylistSquare::class.java)


        setSupportActionBar(toolbarx);
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(true)

        findwidget.setOnQueryTextListener(object:SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {


                return false
            }

            override fun onQueryTextSubmit(query: String?): Boolean {

                bunkerString = query!!

                if (isPlaylist){
                    findPlaylist(bunkerString)
                }else{

                    startSearch(bunkerString)

                }
                return false
            }


        })




        switchtabs.setOnSwitchListener(object: SwitchMultiButton.OnSwitchListener{
            override fun onSwitch(position: Int, tabText: String?) {

                if (position == 0){

                    startSearch(bunkerString)
                    isPlaylist = false
                }

                if (position == 1){

                    findPlaylist(bunkerString)
                    isPlaylist  = true
                }

            }


        })

        nextcon.setOnClickListener {

            next(applicationContext)
        }

        previousncon.setOnClickListener {
            previous(applicationContext)
        }

        playcon.setOnClickListener {

            played()
        }


    }









    fun startSearch(urls:String)
    {


        arraylistx.clear()
        audioList.clear()

        swipes.visibility = View.VISIBLE
        swipes!!.isRefreshing = true
        recyclerview!!.adapter = null


        val pre = "http://api.soundcloud.com/tracks.json?client_id=$keyID&q="
        val post = "&limit=200"
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
                        val ware = FinderPlugAdapter2(applicationContext,arraylistx, recyclerview!!, this@FindView,audioList,playlist!!,constantine)

                        if (ware.itemCount != 0) {
                            recyclerview?.setHasFixedSize(true)
                            recyclerview?.layoutManager =  LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
                            recyclerview!!.adapter = ware  // set adapter on recyclerview
                            ware.notifyDataSetChanged()
                            swipes.isRefreshing = false
                        } else {
                            swipes.isRefreshing = false


                        }
                    }

                    override fun onError(e: Throwable) {



                    }

                    override fun onNext(response: String) {

                        try {

                            val jsonarr = JSONArray(response)

                            for (i in 0..jsonarr.length() - 1) {

                                val jsonobj = jsonarr.getJSONObject(i)


                                val titles = jsonobj.getString("title")
                                val author = jsonobj.getJSONObject("user").getString("username")
                                val art    = jsonobj.getString("artwork_url")
                                val genre =  jsonobj.getString("genre")

                                val link = jsonobj.getString("permalink_url")
                                val ids = jsonobj.getString("id")
                                val player = "https://api.soundcloud.com/tracks/$ids/stream?client_id=$keyID"
                                audioList.add(Audio(player, titles, genre, author, art,link))
                                arraylistx.add(FinderModel2(titles, author, link, art, ids))


                            }


                            swipes.isRefreshing = false;


                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }





                    }
                })

    }





//==================================================PLAYER CONTROLS===================================================//

    fun catfish(){


        bum.visibility = View.VISIBLE
        storage = StorageUtil(applicationContext)
        audioList =  storage!!.loadAudio()
        audioIndex = storage!!.loadAudioIndex()

        if (audioIndex != -1 && audioIndex < audioList!!.size) {
            //index is in a valid range
            activeAudio = audioList!![audioIndex]
            Glide.with(applicationContext).load(activeAudio!!.art!!).into(coverart)
            musicTitle.text = activeAudio!!.title
            musicTitle.isSelected = true
            musicTitle.setTextColor(Color.WHITE)
            artist.text = activeAudio!!.artist

        } else {
            finish()
        }
    }











    override fun onDestroy() {
        super.onDestroy()
        if (observable != null)
        {
            observable!!.unsubscribeOn(Schedulers.io())

        }
    }




    override fun onStop() {
        super.onStop()
        if (observable != null) {
            observable!!.unsubscribeOn(Schedulers.io())
        }
    }








    override fun onResume() {
        super.onResume()

        val broadcastIntent = Intent(Player.Broadcast_IS_PLAYING)
        sendBroadcast(broadcastIntent)
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, IntentFilter("message"))

        if (sounding)
        {
            val broadcastIntents = Intent(Player.Broadcast_PROGRESS_AUDIO)
            sendBroadcast(broadcastIntents)

        }else{


        }
    }





    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
        if (observable != null) {
            observable!!.unsubscribeOn(Schedulers.io())
        }
        super.onPause()
    }

    fun played() {

        if (sounding) {

            val broadcastIntent = Intent(Player.Broadcast_PAUSE_AUDIO)
            sendBroadcast(broadcastIntent)
            playcon.setImageResource(R.drawable.ic_play)
            sounding = false


        } else {


            playcon.setImageResource(R.drawable.ic_pause)
            val broadcastIntent = Intent(Player.Broadcast_PLAY_AUDIO)
            sendBroadcast(broadcastIntent)
            sounding = true

        }

    }







    var mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Extract data included in the Intent
            val mayweather = intent.getStringExtra("messages")
            if (mayweather.contains("playing")) {

                playcon.setImageResource(R.drawable.ic_pause)
                sounding = true


            }


            if (mayweather.contains("pause")) {

                playcon.setImageResource(R.drawable.ic_play)
                sounding = false


            }
            if (mayweather.contains("skip")) {

                val dexs = Integer.parseInt(mayweather.replace("skip", ""))
                audioList = storage!!.loadAudio()
                activeAudio = audioList!![dexs]
                Glide.with(applicationContext).load(activeAudio!!.art).into(coverart)
                musicTitle.text = activeAudio!!.title
                musicTitle.isSelected = true
                artist.text = activeAudio!!.artist
            }
            if (mayweather.contains("nue")) {

                val dexs = Integer.parseInt(mayweather.replace("nue", ""))
                audioList = storage!!.loadAudio()
                activeAudio = audioList!![dexs]
                Glide.with(applicationContext).load(activeAudio!!.art).into(coverart)
                musicTitle.text = activeAudio!!.title
                musicTitle.isSelected = true
                artist.text = activeAudio!!.artist
            }

            if (mayweather.contains("progress")) {


            }

            if (mayweather.contains("duration")) {


            }

            if (mayweather.contains("showplayer")) {

                catfish()

            }

        }
    }





    // Playlist Search



    fun findPlaylist(urls:String)
    {


        arraylistx.clear()
        audioList.clear()
        arrayplay.clear()

        swipes.visibility = View.VISIBLE
        swipes!!.isRefreshing = true
        recyclerview!!.adapter = null


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
                                arrayplay.add(PlayFinderModel(titles, author, link, art, ids,trackcount))


                            }


                            swipes.isRefreshing = false;


                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }


                        val playware = PlayFinder2(applicationContext,arrayplay,playlistSquare!!,constantine,this@FindView)

                        if (playware.itemCount != 0) {
                            recyclerview?.setHasFixedSize(true)
                            recyclerview?.layoutManager =  LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
                            recyclerview!!.adapter = playware  // set adapter on recyclerview
                            playware.notifyDataSetChanged()
                            swipes.isRefreshing = false
                        } else {
                            swipes.isRefreshing = false


                        }


                    }
                })

    }





    fun shareMatrix(audioIndex: Int,audioList:ArrayList<Audio>)
    {

        var activeAudio: Audio? = null
        val storage = StorageUtil(this@FindView)
        storage.storeAudio(audioList)

        if (audioIndex != -1 && audioIndex < audioList!!.size) {
            //index is in a valid range
            activeAudio = audioList!![audioIndex]
        }
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, activeAudio!!.link)
        startActivity(Intent.createChooser(shareIntent, "Share via"))


    }


    fun viewPlaylist(id:String, name:String, image:String, count:String){

        val intents = Intent(this@FindView,PlaylistView::class.java)
        intents.putExtra("id",id)
        intents.putExtra("name",name)
        intents.putExtra("image",image)
        intents.putExtra("count",count)
        startActivity(intents)



    }


}
