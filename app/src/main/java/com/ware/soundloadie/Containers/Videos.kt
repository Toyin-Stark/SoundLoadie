package com.ware.soundloadie.Containers


import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ware.soundloadie.R
import com.ware.soundloadie.UniversalModel.VideoModel
import com.ware.soundloadie.UniversalModel.VideoPlugAdapter
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.videos.*
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import com.bumptech.glide.Glide
import com.ware.soundloadie.App
import com.ware.soundloadie.AudioBox.MediaPlayerService
import com.ware.soundloadie.Dbase.VPlaylist
import com.ware.soundloadie.MainActivity
import com.ware.soundloadie.VideoPlayer
import com.ware.soundloadie.videoBox.Video
import com.ware.soundloadie.videoBox.VideoStorageUtil
import io.objectbox.Box
import kotlinx.android.synthetic.main.genre_row.view.*
import kotlinx.android.synthetic.main.lookup.*
import kotlinx.android.synthetic.main.lookup.view.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit
import org.json.JSONObject
import java.text.SimpleDateFormat


class Videos : Fragment() {
    var observable: Observable<String>? = null
    var wallie:WatchAdapter? = null
    var videoList  =  ArrayList<Video>()
    var videoplaylist: Box<VPlaylist>? = null

    var wave:VideoPlugAdapter? = null
    var list:ArrayList<VideoModel>? = null
    var arraylist = ArrayList<WatchModel>()
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater!!.inflate(R.layout.videos, container, false)

        videoplaylist = (activity.application as App).boxStore.boxFor(VPlaylist::class.java)


        return v
    }


    override fun onStart() {
        super.onStart()


    }















































    fun playVideo(videoIndex:Int){

        killAudioPlayer()
        (activity as MainActivity).hide()

        val storage = VideoStorageUtil(activity)
        storage.storeVideo(videoList)
        storage.storeVideoIndex(videoIndex)

            val vintent = Intent(activity,VideoPlayer::class.java)
            activity.startActivity(vintent)


    }



    override fun onPause() {
        super.onPause()
        if (observable != null) {
            observable!!.unsubscribeOn(Schedulers.io())
        }
    }

    override fun onStop() {
        super.onStop()
        if (observable != null) {
            observable!!.unsubscribeOn(Schedulers.io())
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (observable != null) {
            observable!!.unsubscribeOn(Schedulers.io())
        }
    }


    fun killAudioPlayer(){

        if (!isMyServiceRunning(MediaPlayerService::class.java)) {

        }else{


            val intent = Intent()
            intent.setClass(activity, MediaPlayerService::class.java!!)
            activity.stopService(intent)

        }


    }


    fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }











    fun addPlaylist(videoIndex: Int){
        var activeVideo: Video? = null
        val storage = VideoStorageUtil(activity)
        storage.storeVideo(videoList)

        if (videoIndex != -1 && videoIndex < videoList!!.size) {
            //index is in a valid range
            activeVideo = videoList!![videoIndex]
        }
        val titles =    activeVideo!!.title
        val linkx   =    activeVideo!!.link
        val id     =    activeVideo!!.id
        val times  =    activeVideo!!.published

        val play = VPlaylist(titles = titles,link = linkx, idx = id, published = times)
        videoplaylist!!.put(play)
        snackUp(getString(R.string.added))

    }





    fun snackUp(message:String)
    {


        val snacks = Snackbar.make(constantine!!, message, Snackbar.LENGTH_LONG)
        snacks.view.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorGreen))
        snacks.show()
    }


}


data class WatchModel(val title:String, val desc:String,val imagelink:String)

class WatchAdapter(var c: Context, var lists: ArrayList<WatchModel>, var popular:Videos) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        var v = LayoutInflater.from(c).inflate(R.layout.genre_row, parent, false)
        return Item(v)
    }

    override fun getItemCount(): Int {
        return lists.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as Item).bindData(lists[position],popular)
    }

    class Item(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindData(_list: WatchModel,popular: Videos) {

            val photoUrl = "https://s3-us-west-2.amazonaws.com/soundloop/"+_list.imagelink
            Glide.with(itemView.context).load(photoUrl).into(itemView.coverImage)




            itemView.title.text = _list.title


            itemView.setOnClickListener {



            }
        }
    }
}