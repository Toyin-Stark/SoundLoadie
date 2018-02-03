package com.ware.soundloadie.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.ware.soundloadie.App
import com.ware.soundloadie.AudioBox.Audio
import com.ware.soundloadie.AudioBox.MediaPlayerService
import com.ware.soundloadie.AudioBox.Player
import com.ware.soundloadie.AudioBox.StorageUtil
import com.ware.soundloadie.Dbase.Playlist
import com.ware.soundloadie.R
import com.ware.soundloadie.UniversalModel.MusicModel2
import com.ware.soundloadie.UniversalModel.MusicPlugAdapter2
import com.ware.soundloadie.UniversalModel.PlaylistViewAdapter
import com.ware.soundloadie.UniversalModel.PlaylistViewModel
import com.ware.soundloadie.Utils.next
import com.ware.soundloadie.Utils.previous
import io.objectbox.Box
import io.objectbox.query.Query
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.badge.*
import kotlinx.android.synthetic.main.chart_view_props.*
import kotlinx.android.synthetic.main.controls.*
import kotlinx.android.synthetic.main.playlist_viewers.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class PlaylistView : AppCompatActivity() {
    var adView:AdView? =null

    var observable: Observable<String>? = null
    var arraylistx =  ArrayList<PlaylistViewModel>()
    var mykeys = ""
    var serviceBound = false
    var audioList  =  ArrayList<Audio>()
    private var player: MediaPlayerService? = null
    var playlist: Box<Playlist>? = null
    var playlistQuery: Query<Playlist>? = null

    var sounding = false
    var audioIndex = -1
    var activeAudio: Audio? = null //an object on the currently playing audio
    var storage : StorageUtil? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.playlist_viewers)

        mykeys = getString(R.string.cloudID)
        playlist = (application as App).boxStore.boxFor(Playlist::class.java)


        adView = AdView(this@PlaylistView, getString(R.string.banner), AdSize.BANNER_HEIGHT_50)
        adContainer.addView(adView)
        adView!!.loadAd()


        playlistQuery = playlist!!.query().build()
        setSupportActionBar(toolbar);

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(true)

        val intents = intent.extras
        val name = intents.getString("name")
        val ids  = intents.getString("id")
        val count = intents.getString("count")
        val image = intents.getString("image")
        toolbar.title = name
        val counter = count +" " + getString(R.string.songs)
        sub_title.text = counter
        titlez.text = name
        collapsingToolbar.title = name
        collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE)


        if (image.contains(":")){
            Glide.with(applicationContext).load(image).into(coverImages)

        }else{

            coverImages.setImageResource(R.drawable.playdummy)
        }
        startPlaylist(ids)


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



















    fun startPlaylist(plays:String)
    {
        swipes.isRefreshing = true

        arraylistx.clear()
        audioList.clear()



        recycler_view.adapter = null
        recycler_view.layoutManager =  LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)

        observable = Observable.create(object: ObservableOnSubscribe<String> {
            override fun subscribe(subscriber: ObservableEmitter<String>) {
                try {

                    val client = OkHttpClient().newBuilder()
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS).build()
                    val request = Request.Builder()
                            .url("https://api.soundcloud.com/playlists/$plays?client_id=$mykeys&limit=200&offset=0")
                            .build()
                    val response = client.newCall(request).execute()
                    subscriber.onNext(response.body()!!.string())
                    subscriber.onComplete()
                    if (!response.isSuccessful)
                        subscriber.onError(Exception(response.toString()))
                } catch (e: IOException) {
                    subscriber.onError(e)
                }
            }
        })

        observable!!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: Observer<String> {
                    override fun onSubscribe(d: Disposable) {


                    }

                    override fun onComplete() {

                        val wave = PlaylistViewAdapter(applicationContext,arraylistx, audioList,playlist!!,constantines,this@PlaylistView)

                        if (wave.itemCount != 0) {

                            recycler_view.adapter = wave// set adapter on recyclerview
                            wave.notifyDataSetChanged()

                            swipes.isRefreshing = false

                        } else {
                            swipes.isRefreshing = false
                            Toast.makeText(applicationContext,"lala", Toast.LENGTH_LONG).show()


                        }


                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(applicationContext,""+e.message, Toast.LENGTH_LONG).show()

                    }

                    override fun onNext(response: String) {

                        try {

                            val json = JSONObject(response)


                            val jsonarr = json.getJSONArray("tracks")

                            for (i in 0..jsonarr.length() - 1) {

                                var jsonobj = jsonarr.getJSONObject(i)


                                val titles = jsonobj.getString("title")
                                val author = jsonobj.getJSONObject("user").getString("username")
                                val art = jsonobj.getString("artwork_url")
                                val genre = jsonobj.getString("genre")
                                val link = jsonobj.getString("permalink_url")
                                val ids = jsonobj.getString("id")
                                val player = "https://api.soundcloud.com/tracks/$ids/stream?client_id=$mykeys"
                                audioList.add(Audio(player, titles, genre, author, art,link))
                                arraylistx.add(PlaylistViewModel(titles, author, link, art, ids))

                            }



                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }




                    }
                })

    }








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




    fun shareMatrix(audioIndex: Int)
    {

        var activeAudio: Audio? = null
        val storage = StorageUtil(this@PlaylistView)
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










}
