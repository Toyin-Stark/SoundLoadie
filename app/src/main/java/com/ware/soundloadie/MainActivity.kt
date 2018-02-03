package com.ware.soundloadie

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.bumptech.glide.Glide
import com.ware.soundloadie.AudioBox.Audio
import com.ware.soundloadie.AudioBox.Player.Companion.Broadcast_IS_PLAYING
import com.ware.soundloadie.AudioBox.Player.Companion.Broadcast_PAUSE_AUDIO
import com.ware.soundloadie.AudioBox.Player.Companion.Broadcast_PLAY_AUDIO
import com.ware.soundloadie.AudioBox.Player.Companion.Broadcast_PLAY_NEXT_AUDIO
import com.ware.soundloadie.AudioBox.Player.Companion.Broadcast_PLAY_PREV_AUDIO
import com.ware.soundloadie.AudioBox.Player.Companion.Broadcast_PROGRESS_AUDIO
import com.ware.soundloadie.AudioBox.StorageUtil
import com.ware.soundloadie.Containers.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.badge.*
import kotlinx.android.synthetic.main.controls.*
import com.ware.soundloadie.Tabs.ViewPagerAdapter
import android.support.v4.view.ViewPager
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import com.ware.soundloadie.Travel.Lecturer
import com.ware.soundloadie.Utils.next
import com.ware.soundloadie.Utils.previous


class MainActivity : AppCompatActivity() {

    var mInterstitialAd: InterstitialAd? = null

    var sounding = false
    var audioList: ArrayList<Audio>? = null
    var audioIndex = -1
    var activeAudio: Audio? = null //an object on the currently playing audio
    var storage : StorageUtil? = null
    private val tabIcons = intArrayOf(R.drawable.ic_home,R.drawable.ic_search, R.drawable.ic_bolt, R.drawable.ic_heart_white, R.drawable.ic_info)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Starters()

        setupViewPager(viewpager);
        tabs.setupWithViewPager(viewpager)
        setupTabIcons()


        mInterstitialAd = InterstitialAd(this@MainActivity,getString(R.string.intersistal))



        mInterstitialAd!!.setAdListener(object : InterstitialAdListener{
            override fun onLoggingImpression(p0: Ad?) {


            }

            override fun onAdLoaded(p0: Ad?) {

                mInterstitialAd!!.show();

            }

            override fun onError(p0: Ad?, p1: AdError?) {


            }

            override fun onInterstitialDismissed(p0: Ad?) {


            }

            override fun onAdClicked(p0: Ad?) {


            }

            override fun onInterstitialDisplayed(p0: Ad?) {


            }


        })



        // Load ads into Interstitial Ads
        mInterstitialAd!!.loadAd()





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






    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(Charts(), "")
        adapter.addFragment(Search(), "")
        adapter.addFragment(Discover(), "")
        adapter.addFragment(Playpage(), "")
        adapter.addFragment(Learner(),"")
        viewPager.adapter = adapter
    }


    private fun setupTabIcons() {
        tabs.getTabAt(0)!!.setIcon(tabIcons[0])
        tabs.getTabAt(1)!!.setIcon(tabIcons[1])
        tabs.getTabAt(2)!!.setIcon(tabIcons[2])
        tabs.getTabAt(3)!!.setIcon(tabIcons[3])
        tabs.getTabAt(4)!!.setIcon(tabIcons[4])

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
            musicTitle.setTextColor(Color.WHITE)
            musicTitle.isSelected = true
            artist.text = activeAudio!!.artist

        } else {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        val broadcastIntent = Intent(Broadcast_IS_PLAYING)
        sendBroadcast(broadcastIntent)
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, IntentFilter("message"))

        if (sounding)
        {
            val broadcastIntent = Intent(Broadcast_PROGRESS_AUDIO)
            sendBroadcast(broadcastIntent)

        }else{


        }
    }





    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
        super.onPause()
    }

    fun played() {

        if (sounding) {

            val broadcastIntent = Intent(Broadcast_PAUSE_AUDIO)
            sendBroadcast(broadcastIntent)
            playcon.setImageResource(R.drawable.ic_play)
            sounding = false


        } else {


            playcon.setImageResource(R.drawable.ic_pause)
            val broadcastIntent = Intent(Broadcast_PLAY_AUDIO)
            sendBroadcast(broadcastIntent)
            sounding = true

        }

    }




    fun hide(){

        bum.visibility = View.GONE
    }








    var mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Extract data included in the Intent
            val mayweather = intent.getStringExtra("messages")
            if (mayweather.contains("playing")) {

                playcon.setImageResource(R.drawable.ic_pause)
                sounding = true

                if (bum.visibility  == View.VISIBLE){

                }else{

                    catfish()

                }



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





    fun Starters(){

        val t = Thread(Runnable {
            //  Initialize SharedPreferences
            val getPrefs = PreferenceManager
                    .getDefaultSharedPreferences(baseContext)

            //  Create a new boolean and preference and set it to true
            val isFirstStart = getPrefs.getBoolean("firstStart", true)

            //  If the activity has never started before...
            if (isFirstStart) {

                //  Launch app intro
                val i = Intent(this@MainActivity, Lecturer::class.java)

                runOnUiThread { startActivity(i) }

                //  Make a new preferences editor
                val e = getPrefs.edit()

                //  Edit preference to make it false because we don't want this to run again
                e.putBoolean("firstStart", false)

                //  Apply changes
                e.apply()
            }
        })

        // Start the thread
        t.start()

    }

}
