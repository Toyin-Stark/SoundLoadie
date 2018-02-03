package com.ware.soundloadie

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.pierfrancescosoffritti.youtubeplayer.player.YouTubePlayer
import com.pierfrancescosoffritti.youtubeplayer.player.YouTubePlayerInitListener
import com.prof.youtubeparser.models.stats.Statistics
import com.prof.youtubeparser.VideoStats
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.video_band_layout.*
import kotlinx.android.synthetic.main.video_player.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit
import com.pierfrancescosoffritti.youtubeplayer.player.AbstractYouTubePlayerListener
import org.json.JSONObject
import android.os.AsyncTask.execute
import android.support.annotation.Nullable
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.pierfrancescosoffritti.youtubeplayer.player.YouTubePlayerListener
import com.ware.soundloadie.videoBox.Video
import com.ware.soundloadie.videoBox.VideoStorageUtil
import kotlinx.android.synthetic.main.badge.*
import kotlinx.android.synthetic.main.controls.*


class VideoPlayer : AppCompatActivity() {

    val API_KEY = "AIzaSyAIlhBQLr9qBk9UOJHvU-E5rbxCwvtEOZ4"
    var observable: Observable<String>? = null
    @Nullable private var initializedYouTubePlayer: YouTubePlayer? = null
    val videoStats = VideoStats()
    var index = ""
    var videoList  =  ArrayList<Video>()
    var videoIndex = -1
    var isPlaying = false
    var isBufferring = false
    private var activeVideo: Video? = null
    var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_player)

        mInterstitialAd = InterstitialAd(this@VideoPlayer)
        mInterstitialAd!!.adUnitId = getString(R.string.intersistal)

        val adRequest = AdRequest.Builder()
                .build()

        // Load ads into Interstitial Ads
        mInterstitialAd!!.loadAd(adRequest)

        mInterstitialAd!!.adListener = object : AdListener() {
            override fun onAdLoaded() {

                showInterstitial()

                val adRequest = AdRequest.Builder()
                        .build()

                // Load ads into Interstitial Ads
                mInterstitialAd!!.loadAd(adRequest)
            }

            override fun onAdClosed() {
                super.onAdClosed()
            }
        }





        val storage = VideoStorageUtil(applicationContext)
        videoList = storage.loadVideo()
        videoIndex = storage.loadVideoIndex()

        if (videoIndex != -1 && videoIndex <videoList!!.size) {
            //index is in a valid range
            activeVideo = videoList!![videoIndex]

            val vID = activeVideo!!.id
            musicTitle.text = activeVideo!!.title
            toolbar.title =  activeVideo!!.title

            setSupportActionBar(toolbar);
            toolbar.setTitleTextColor(ContextCompat.getColor(this@VideoPlayer,android.R.color.white))

            supportActionBar!!.setDisplayHomeAsUpEnabled(true);

            youTubePlayerView.initialize({ initializedYouTubePlayer ->
                initializedYouTubePlayer.addListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady() {

                        this@VideoPlayer.initializedYouTubePlayer = initializedYouTubePlayer
                        initializedYouTubePlayer.addListener(this)

                        initializedYouTubePlayer.loadVideo(vID,0f)

                    }

                    override fun onStateChange(state: Int) {
                        super.onStateChange(state)
                        when(state) {

                            0 -> {

                                skipToNext()
                            }

                            1 -> {

                                playcon.visibility = View.VISIBLE
                                isPlaying = true
                                playcon.setImageResource(R.drawable.ic_pause)

                            }

                            2 -> {

                                playcon.visibility = View.VISIBLE
                                playcon.setImageResource(R.drawable.ic_play)
                                isPlaying = false


                            }

                            3 -> {


                                playcon.visibility = View.GONE
                                isBufferring = true
                            }

                            5 -> {

                            }
                        }
                    }
                })
            }, true)

            startVideo(vID)

        } else {

            finish()
        }




        playcon.setOnClickListener {

            if (isPlaying){

                if (initializedYouTubePlayer != null)
                    initializedYouTubePlayer!!.pause()



            }

            if (!isPlaying){

                if (initializedYouTubePlayer != null)
                    initializedYouTubePlayer!!.play()

            }

        }

        previousncon.setOnClickListener {

            skipToPrevious()
        }


        nextcon.setOnClickListener {

            skipToNext()

        }




    }



    fun showInterstitial() {
        if (mInterstitialAd!!.isLoaded()) {
            mInterstitialAd!!.show();
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.video_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item!!.itemId){

            R.id.watch ->{

                watchYoutubeVideo(activeVideo!!.id)

            }

            R.id.open ->{

                Browsers(activeVideo!!.id)

            }

            R.id.share ->{


            }

        }

        return super.onOptionsItemSelected(item)

    }



    fun startVideo(videoID:String)
    {


        val imageUrl = "http://img.youtube.com/vi/$videoID/maxresdefault.jpg"
        Glide.with(this@VideoPlayer).load(imageUrl).into(coverart)
        spinner.visibility = View.VISIBLE
        artist.text = "..."
        toolbar.title =  activeVideo!!.title

        youTubePlayerView.enterFullScreen()
        observable = Observable.create(object: ObservableOnSubscribe<String> {
            override fun subscribe(subscriber: ObservableEmitter<String>) {
                try {


                    val client = OkHttpClient().newBuilder()
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS).build()
                    val request = Request.Builder()
                            .url("https://www.googleapis.com/youtube/v3/videos?part=snippet&id=$videoID&fields=items(id%2Csnippet)&key=AIzaSyAIlhBQLr9qBk9UOJHvU-E5rbxCwvtEOZ4")
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

                        spinner.visibility = View.GONE
                        if (initializedYouTubePlayer != null)
                            initializedYouTubePlayer!!.loadVideo(videoID,0f)

                    }

                    override fun onError(e: Throwable) {


                    }

                    override fun onNext(response: String) {




                        val json = JSONObject(response)


                        val jsonarr = json.getJSONArray("items")

                        for (i in 0..jsonarr.length() - 1) {

                            var jsonobj = jsonarr.getJSONObject(i)




                            val author = jsonobj.getJSONObject("snippet").getString("channelTitle")
                            artist.text = author



                        }



                    }
                })

    }



    fun skipToNext(){

        if (videoIndex == videoList!!.size - 1) {
            //if last in playlist
           videoIndex = 0
            activeVideo = videoList!![videoIndex]
        } else {
            //get next in playlist
            activeVideo = videoList!![++videoIndex]

        }

        if(isPlaying){
            if (initializedYouTubePlayer != null)
            initializedYouTubePlayer!!.pause()
        }
        VideoStorageUtil(applicationContext).storeVideoIndex(videoIndex)
        val vID = activeVideo!!.id
        musicTitle.text = activeVideo!!.title
        startVideo(vID)
    }





   fun watchYoutubeVideo(id:String){

       val appIntent =  Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id))
       val webIntent =  Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + id))
       try {
        startActivity(appIntent);
        } catch ( ex: ActivityNotFoundException) {
        startActivity(webIntent);
       }

   }



    fun Browsers(id:String){

        val browserIntent =  Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$id"));
        startActivity(browserIntent)
    }






    fun skipToPrevious(){

        if (videoIndex == videoList!!.size - 1) {
            //if last in playlist
            videoIndex = 0
            activeVideo = videoList!![videoIndex]
        } else {
            //get next in playlist
            activeVideo = videoList!![--videoIndex]

        }

        //Update stored index

        if(isPlaying){
            if (initializedYouTubePlayer != null)
                initializedYouTubePlayer!!.pause()
        }
        VideoStorageUtil(applicationContext).storeVideoIndex(videoIndex)
        val vID = activeVideo!!.id
        musicTitle.text = activeVideo!!.title
        startVideo(vID)
    }


    override fun onDestroy() {
        super.onDestroy()
        youTubePlayerView.release()
    }


    override fun onPause() {
        super.onPause()
        if (initializedYouTubePlayer != null)
            initializedYouTubePlayer!!.pause()
    }
}
