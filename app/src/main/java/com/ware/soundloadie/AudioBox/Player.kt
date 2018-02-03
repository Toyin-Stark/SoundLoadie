package com.ware.soundloadie.AudioBox

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.PersistableBundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import com.bumptech.glide.Glide
import com.ware.soundloadie.R


import java.util.ArrayList
import java.util.Timer
import java.util.TimerTask
import kotlinx.android.synthetic.main.player.*


class Player : AppCompatActivity() {


    var player: MediaPlayerService? = null
    var serviceBound = false
    var sounding = false
    var audioList: ArrayList<Audio>? = null
    var audioIndex = -1
    var activeAudio: Audio? = null //an object on the currently playing audio
    var count:Int?=null

    var storage : StorageUtil? = null
    var h: Handler? = null
    var delay: Int = 0
    var runnable: Runnable? = null
    var durex = 0
    var wheres = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.player)


        seekbar.progress = 0
        storage = StorageUtil(applicationContext)
        audioList = storage!!.loadAudio()
        audioIndex = storage!!.loadAudioIndex()

        if (audioIndex != -1 && audioIndex < audioList!!.size) {
            //index is in a valid range
            activeAudio = audioList!![audioIndex]
            Glide.with(applicationContext).load(activeAudio!!.art!!.replace("large", "t500x500")).into(cover)
            titled.text = activeAudio!!.title
            artiste.text = activeAudio!!.artist

        } else {
            finish()
        }




        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {


            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val broadcastIntent = Intent(Broadcast_SEEK_AUDIO)
                broadcastIntent.putExtra("skipto", seekBar.progress)
                sendBroadcast(broadcastIntent)

            }
        })

        Polls()

    }


    override fun onStart() {
        val broadcastIntent = Intent(Broadcast_IS_PLAYING)
        sendBroadcast(broadcastIntent)


        super.onStart()
    }

    override fun onResume() {
        super.onResume()

        val broadcastIntent = Intent(Broadcast_IS_PLAYING)
        sendBroadcast(broadcastIntent)
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, IntentFilter("message"))

        if (sounding)
        {
            seekbar.progress = 0
            val broadcastIntent = Intent(Broadcast_PROGRESS_AUDIO)
            sendBroadcast(broadcastIntent)

        }else{
            seekbar.progress = 0
        }
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
        super.onPause()
    }

    fun played(view: View) {

        if (sounding) {

            val broadcastIntent = Intent(Broadcast_PAUSE_AUDIO)
            sendBroadcast(broadcastIntent)
            playIcon.setImageResource(R.drawable.ic_play)
            sounding = false


        } else {


            playIcon.setImageResource(R.drawable.ic_pause)
            val broadcastIntent = Intent(Broadcast_PLAY_AUDIO)
            sendBroadcast(broadcastIntent)
            sounding = true

        }

    }

    fun previous(view: View) {


        //Service is active
        //Send a broadcast to the service -> PLAY_NEW_AUDIO
        val broadcastIntent = Intent(Broadcast_PLAY_PREV_AUDIO)
        sendBroadcast(broadcastIntent)
        seekbar.progress = 0
        timer.text = "00:00"
        tick.text  = "00:00"
    }

    fun next(view: View) {


        val broadcastIntent = Intent(Broadcast_PLAY_NEXT_AUDIO)
        sendBroadcast(broadcastIntent)


        seekbar.progress = 0
        timer.text = "00:00"
        tick.text  = "00:00"
    }


    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putBoolean("serviceStatus", serviceBound)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        serviceBound = savedInstanceState.getBoolean("serviceStatus")
    }

    //Binding this Client to the AudioPlayer Service






    var mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Extract data included in the Intent
            val mayweather = intent.getStringExtra("messages")
            if (mayweather.contains("playing")) {

                playIcon.setImageResource(R.drawable.ic_pause)
                sounding = true


            }


            if (mayweather.contains("pause")) {

                playIcon.setImageResource(R.drawable.ic_play)
                sounding = false


            }
            if (mayweather.contains("skip")) {

                val dexs = Integer.parseInt(mayweather.replace("skip", ""))
                audioList = storage!!.loadAudio()
                activeAudio = audioList!![dexs]
                Glide.with(applicationContext).load(activeAudio!!.art).into(cover)
                titled.text = activeAudio!!.title
                artiste.text = activeAudio!!.artist
            }

            if (mayweather.contains("progress")) {
                count = Integer.parseInt(mayweather.replace("progress", ""))
                val now = (durex - count!!).toLong()
                val tuck = getTimeString(count!!.toLong())
                val duracell = getTimeString(now)
                timer.text = "" + duracell
                tick.text = ""+tuck
                seekbar.progress = count!!


            }

            if (mayweather.contains("duration")) {
                durex = Integer.parseInt(mayweather.replace("duration", ""))
                seekbar.max = durex

            }

        }
    }


    fun Polls() {

        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {


                if (sounding) {
                    val broadcastIntent = Intent(Broadcast_PROGRESS_AUDIO)
                    sendBroadcast(broadcastIntent)

                } else {


                }


            }
        }, 0, 1000)


    }


    private fun getTimeString(millis: Long): String {
        val buf = StringBuffer()

        val hours = (millis / (1000 * 60 * 60)).toInt()
        val minutes = (millis % (1000 * 60 * 60) / (1000 * 60)).toInt()
        val seconds = (millis % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()

        buf
                .append(String.format("%02d", hours))
                .append(":")
                .append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds))

        return buf.toString()
    }

    companion object {
        val Broadcast_PLAY_NEW_AUDIO = "com.ware.soundloadie.PlayNewAudio"
        val Broadcast_PLAY_NEXT_AUDIO = "com.ware.soundloadie.PlayNextAudio"
        val Broadcast_PLAY_PREV_AUDIO = "com.ware.soundloadie.PlayPrevAudio"
        val Broadcast_PAUSE_AUDIO =     "com.ware.soundloadie.PauseAudio"
        val Broadcast_PLAY_AUDIO  =     "com.ware.soundloadie.PlayAudio"
        val Broadcast_PROGRESS_AUDIO =  "com.ware.soundloadie.ProgressAudio"
        val Broadcast_SEEK_AUDIO =      "com.ware.soundloadie.SeekAudio"
        val Broadcast_IS_PLAYING =      "com.ware.soundloadie.IsPlaying"
        val Broadcast_DESTROY_PLAYER = "com.ware.soundloadie.DestroyPlayer"
        val Broadcast_SHOW_PLAYER = "com.ware.soundloadie.ShowPlayer"

    }
}
