package com.ware.soundloadie.Containers


import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ware.soundloadie.App
import com.ware.soundloadie.AudioBox.Audio
import com.ware.soundloadie.AudioBox.MediaPlayerService
import com.ware.soundloadie.AudioBox.StorageUtil
import com.ware.soundloadie.Containers.Charts.Companion.Broadcast_PLAY_NEW_AUDIO
import com.ware.soundloadie.Dbase.Playlist
import com.ware.soundloadie.Dbase.PlaylistSquare
import com.ware.soundloadie.Dbase.VPlaylist
import com.ware.soundloadie.MainActivity

import com.ware.soundloadie.R
import com.ware.soundloadie.UniversalModel.*
import com.ware.soundloadie.VideoPlayer
import com.ware.soundloadie.WebBox.Browsers
import com.ware.soundloadie.activities.PlaylistView
import com.ware.soundloadie.videoBox.Video
import com.ware.soundloadie.videoBox.VideoStorageUtil
import io.objectbox.Box
import io.objectbox.query.Query
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.misc_row_b.*
import kotlinx.android.synthetic.main.playpage.*
import kotlinx.android.synthetic.main.playpage.view.*
import lib.kingja.switchbutton.SwitchMultiButton
import java.text.FieldPosition


class Playpage : Fragment() {

    var list:ArrayList<PlayModel>? = null
    var vlist:ArrayList<VideoPlayModel>? = null
    var misclist:ArrayList<MiscModel>? = null
    var observable: Observable<String>? = null
    var audioList  =  ArrayList<Audio>()
    var videoList  =  ArrayList<Video>()

    var playlistQuery: Query<Playlist>? = null
    var videoplaylistQuery: Query<VPlaylist>? = null
    var playlist: Box<Playlist>? = null
    var videoplaylist: Box<VPlaylist>? = null
    var miscplaylist: Box<PlaylistSquare>? = null
    var miscplaylistQuery: Query<PlaylistSquare>? = null

    var wave:PlayPlugAdapter? = null
    var videowave: VideoPlayPlugAdapter? = null
    var miscwave:  MiscFinder? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v =  inflater!!.inflate(R.layout.playpage, container, false)




        v.switchbox.setOnSwitchListener(object:SwitchMultiButton.OnSwitchListener{
            override fun onSwitch(position: Int, tabText: String?) {

                if (position == 0){

                    startAudio()
                }

                if (position == 1){

                    startPlaylist()
                }

                if (position == 2){

                    startVideo()
                }

            }


        })



        return v
    }





    //=================================================================================================================================================//
                                                             //---AUDIO--//
    //=================================================================================================================================================//




    fun startAudio()
    {

        heart_break.visibility = View.GONE
        playlist = (activity.application as App).boxStore.boxFor(Playlist::class.java)
        playlistQuery = playlist!!.query().build()
        swipe_view.isRefreshing = true
        list = ArrayList<PlayModel>()

        val shuffle = playlistQuery!!.find()
        list!!.clear()
        audioList.clear()



        recyclerview.adapter = null
        recyclerview.isNestedScrollingEnabled = true
        recyclerview.layoutManager =  LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        observable = Observable.create(object: ObservableOnSubscribe<String> {
            override fun subscribe(subscriber: ObservableEmitter<String>) {


                for (i in 0 until shuffle.size){

                    val player = shuffle[i].player
                    val titles = shuffle[i].titles
                    val genre  = shuffle[i].genre
                    val author = shuffle[i].author
                    val art    = shuffle[i].art
                    val link   = shuffle[i].link

                            audioList.add(Audio(player, titles, genre, author, art,link))
                            list!!.add(PlayModel(titles!!, author!!, link!!, art!!))
                }

                subscriber.onNext("")
                subscriber.onComplete()

            }
        })

        observable!!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: Observer<String> {
                    override fun onSubscribe(d: Disposable) {


                    }

                    override fun onComplete() {

                         wave = PlayPlugAdapter(activity,list!!,this@Playpage)

                        if (wave!!.itemCount != 0) {
                            heart_break.visibility = View.GONE
                            recyclerview.adapter = wave// set adapter on recyclerview
                            wave!!.notifyDataSetChanged()

                            swipe_view.isRefreshing = false

                        } else {
                            swipe_view.isRefreshing = false
                            heart_break.visibility = View.VISIBLE


                        }


                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(activity,""+e.message,Toast.LENGTH_LONG).show()

                    }

                    override fun onNext(response: String) {



                    }
                })

    }


    //=================================================================================================================================================//
                                                         //--- VIDEOS--//
    //=================================================================================================================================================//





    fun startVideo()
    {

        heart_break.visibility = View.GONE
        videoplaylist = (activity.application as App).boxStore.boxFor(VPlaylist::class.java)
        videoplaylistQuery = videoplaylist!!.query().build()
        swipe_view.isRefreshing = true
        vlist = ArrayList<VideoPlayModel>()
        val shuffle = videoplaylistQuery!!.find()
        vlist!!.clear()
        videoList.clear()



        recyclerview.adapter = null
        recyclerview.isNestedScrollingEnabled = true
        recyclerview.layoutManager =  LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        observable = Observable.create(object: ObservableOnSubscribe<String> {
            override fun subscribe(subscriber: ObservableEmitter<String>) {


                for (i in 0 until shuffle.size){

                    val titles = shuffle[i].titles
                    val links  = shuffle[i].link
                    val ids = shuffle[i].idx
                    val times    = shuffle[i].published

                    videoList.add(Video(titles!!, links!!,ids!!, times!!))
                    vlist!!.add(VideoPlayModel(titles, links,ids, times))
                }

                subscriber.onNext("")
                subscriber.onComplete()

            }
        })

        observable!!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: Observer<String> {
                    override fun onSubscribe(d: Disposable) {


                    }

                    override fun onComplete() {

                        videowave = VideoPlayPlugAdapter(activity,vlist!!,this@Playpage)

                        if ( videowave!!.itemCount != 0) {
                            heart_break.visibility = View.GONE
                            recyclerview.adapter =  videowave// set adapter on recyclerview
                            videowave!!.notifyDataSetChanged()

                            swipe_view.isRefreshing = false

                        } else {
                            swipe_view.isRefreshing = false
                            heart_break.visibility = View.VISIBLE


                        }


                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(activity,""+e.message,Toast.LENGTH_LONG).show()

                    }

                    override fun onNext(response: String) {



                    }
                })

    }


















    //=================================================================================================================================================//
                                                              //---MISC--//
    //=================================================================================================================================================//





    fun startPlaylist()
    {

        heart_break.visibility = View.GONE
        miscplaylist = (activity.application as App).boxStore.boxFor(PlaylistSquare::class.java)
        miscplaylistQuery = miscplaylist!!.query().build()
        swipe_view.isRefreshing = true
        misclist = ArrayList<MiscModel>()
        val shuffle = miscplaylistQuery!!.find()
        misclist!!.clear()




        recyclerview.adapter = null
        recyclerview.isNestedScrollingEnabled = true
        recyclerview.layoutManager =  LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        observable = Observable.create(object: ObservableOnSubscribe<String> {
            override fun subscribe(subscriber: ObservableEmitter<String>) {


                for (i in 0 until shuffle.size){

                    val titles = shuffle[i].titles
                    val links  = shuffle[i].link
                    val ids    = shuffle[i].idx
                    val image  = shuffle[i].art
                    val type   = shuffle[i].mime
                    val author = shuffle[i].author

                    misclist!!.add(MiscModel(titles!!,author!!,links!!,image!!,ids!!,type!!))
                }

                subscriber.onNext("")
                subscriber.onComplete()

            }
        })

        observable!!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: Observer<String> {
                    override fun onSubscribe(d: Disposable) {


                    }

                    override fun onComplete() {

                        miscwave = MiscFinder(activity,misclist!!,miscplaylist!!,constantine,this@Playpage)

                        if (miscwave!!.itemCount != 0) {
                            heart_break.visibility = View.GONE
                            recyclerview.adapter = miscwave// set adapter on recyclerview
                            miscwave!!.notifyDataSetChanged()

                            swipe_view.isRefreshing = false

                        } else {
                            swipe_view.isRefreshing = false
                            heart_break.visibility = View.VISIBLE


                        }


                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(activity,""+e.message,Toast.LENGTH_LONG).show()

                    }

                    override fun onNext(response: String) {



                    }
                })

    }


    fun viewPlaylist(id:String, name:String, image:String, count:String){

        val intents = Intent(activity, PlaylistView::class.java)
        intents.putExtra("id",id)
        intents.putExtra("name",name)
        intents.putExtra("image",image)
        intents.putExtra("count",count)
        startActivity(intents)



    }



























    override fun onStart() {
        super.onStart()
        startAudio()
    }


    override fun onResume() {
        super.onResume()
    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (isVisibleToUser) {


        }
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



    fun playAudio(audioIndex: Int) {

        if (!isMyServiceRunning(MediaPlayerService::class.java)) {
            //Store Serializable audioList to SharedPreferences
            val storage = StorageUtil(activity)
            storage.storeAudio(audioList)
            storage.storeAudioIndex(audioIndex)

            val playerIntent = Intent(activity, MediaPlayerService::class.java)
            activity.startService(playerIntent)
            (activity as MainActivity).catfish()


        } else {
            //Store the new audioIndex to SharedPreferences
            val storage = StorageUtil(activity)
            storage.storeAudio(audioList)
            storage.storeAudioIndex(audioIndex)

            val broadcastIntent = Intent(Broadcast_PLAY_NEW_AUDIO)
            broadcastIntent.putExtra("messages", "nue"+audioIndex)

            activity.sendBroadcast(broadcastIntent)
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


    fun shareMatrix(audioIndex: Int)
    {

        var activeAudio: Audio? = null
        val storage = StorageUtil(activity)
        storage.storeAudio(audioList)

        if (audioIndex != -1 && audioIndex < audioList!!.size) {
            //index is in a valid range
            activeAudio = audioList!![audioIndex]
        }
        val shareIntent = Intent(android.content.Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, activeAudio!!.link)
        activity.startActivity(Intent.createChooser(shareIntent, "Share via"))


    }


    fun playVideo(videoIndex:Int){

        killAudioPlayer()
        (activity as MainActivity).hide()

        val storage = VideoStorageUtil(activity)
        storage.storeVideo(videoList)
        storage.storeVideoIndex(videoIndex)

        val vintent = Intent(activity, VideoPlayer::class.java)
        activity.startActivity(vintent)


    }



    fun killAudioPlayer(){

        if (!isMyServiceRunning(MediaPlayerService::class.java)) {

        }else{


            val intent = Intent()
            intent.setClass(activity, MediaPlayerService::class.java!!)
            activity.stopService(intent)

        }


    }




    fun Webpost(audioIndex: Int)
    {


        var activeAudio: Audio? = null
        val storage = StorageUtil(activity)
        storage.storeAudio(audioList)

        if (audioIndex != -1 && audioIndex < audioList!!.size) {
            //index is in a valid range
            activeAudio = audioList!![audioIndex]
        }
        val intent = Intent(activity, Browsers::class.java)
        intent.putExtra("url",activeAudio!!.link)
        intent.putExtra("name",activeAudio!!.title)

        activity.startActivity(intent)

    }


    fun remover(audioIndex: Int){
        val playnote = playlistQuery!!.find()
        val gone  = playnote[audioIndex]
        playlist!!.remove(gone)
        list!!.removeAt(audioIndex)
        wave!!.notifyDataSetChanged()

    }



    fun videoremover(videoIndex: Int){
        val playnote = videoplaylistQuery!!.find()
        val gone  = playnote[videoIndex]
        videoplaylist!!.remove(gone)
        vlist!!.removeAt(videoIndex)
        videowave!!.notifyDataSetChanged()

    }



    fun playlistRemover(position:Int){

        val playnote = miscplaylistQuery!!.find()
        val gone  = playnote[position]
        miscplaylist!!.remove(gone)
        misclist!!.removeAt(position)
        miscwave!!.notifyDataSetChanged()
    }
}// Required empty public constructor
