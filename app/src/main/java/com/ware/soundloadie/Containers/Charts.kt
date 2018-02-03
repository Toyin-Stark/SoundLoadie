package com.ware.soundloadie.Containers

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.genre_row.view.*
import android.support.v7.widget.LinearLayoutManager
import android.support.v4.widget.SwipeRefreshLayout
import android.widget.Toast
import com.ware.soundloadie.App
import com.ware.soundloadie.AudioBox.Audio
import com.ware.soundloadie.AudioBox.MediaPlayerService
import com.ware.soundloadie.AudioBox.StorageUtil
import com.ware.soundloadie.Dbase.Playlist
import com.ware.soundloadie.MainActivity
import com.ware.soundloadie.R
import com.ware.soundloadie.UniversalModel.*
import com.ware.soundloadie.Utils.readFile
import com.ware.soundloadie.WebBox.Browsers
import io.objectbox.Box
import io.objectbox.query.Query
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.chart_recycler_1.*
import kotlinx.android.synthetic.main.chart_recycler_2.*
import kotlinx.android.synthetic.main.genre.*
import kotlinx.android.synthetic.main.genre.view.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit


class Charts : Fragment(),SwipeRefreshLayout.OnRefreshListener {


    companion object {
        val Broadcast_PLAY_NEW_AUDIO = "com.ware.soundloadie.PlayNewAudio";

    }
    var hot: HotAdapter? = null
    var hotlist:ArrayList<HotModel>? = null
    var observable: Observable<String>? = null
    var chatobservable: Observable<String>? = null

    var nue: NewAdapter? = null
    var nuelist:ArrayList<NewModel>? = null
    var mykeys = ""







    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val v = inflater!!.inflate(R.layout.genre, container, false)

        v.swipes.setOnRefreshListener(this)
        mykeys = getString(R.string.cloudID)

        return v
    }





    override fun onStart() {
        super.onStart()
        musicCharts()
        audioCharts()
    }





    override fun onRefresh() {


    }






    fun musicCharts()
    {
        hotlist = ArrayList<HotModel>()
        recyclerView1.adapter = null
        recyclerView1.isNestedScrollingEnabled = true
        recyclerView1.layoutManager =  LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        observable = Observable.create(object: ObservableOnSubscribe<String> {
            override fun subscribe(subscriber: ObservableEmitter<String>) {


                val response = readFile("data/charts_data.json",activity)

                subscriber.onNext(response)
                subscriber.onComplete()

            }
        })

        observable!!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: Observer<String> {
                    override fun onSubscribe(d: Disposable) {


                    }

                    override fun onComplete() {
                        hot = HotAdapter(activity, hotlist!!, this@Charts)
                        recyclerView1.adapter = hot
                        hot!!.notifyDataSetChanged()
                        swipes.isRefreshing = false

                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(activity,""+e.message,Toast.LENGTH_LONG).show()

                    }

                    override fun onNext(response: String) {
                        val json = JSONObject(response)
                        val jsonarr = json.getJSONArray("data")

                        for (i in 0..jsonarr.length() - 1) {

                            val jsonobj = jsonarr.getJSONObject(i)

                            val name = jsonobj.getString("name")
                            val links = jsonobj.getString("code")
                            val images = jsonobj.getString("images")

                            hotlist!!.add(HotModel(name,links,images))

                        }


                    }
                })

    }




   // ===============================================================================================================
    // Load Top Audio charts
  //=================================================================================================================



    fun audioCharts()
    {
        nuelist = ArrayList<NewModel>()
        recyclerView.adapter = null
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.layoutManager =  LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        chatobservable = Observable.create(object: ObservableOnSubscribe<String> {
            override fun subscribe(subscriber: ObservableEmitter<String>) {


                val response = readFile("data/audio_data.json",activity)

                subscriber.onNext(response)
                subscriber.onComplete()

            }
        })

        chatobservable!!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: Observer<String> {
                    override fun onSubscribe(d: Disposable) {


                    }

                    override fun onComplete() {
                        nue = NewAdapter(activity, nuelist!!, this@Charts)
                        recyclerView.adapter = nue
                        nue!!.notifyDataSetChanged()
                        swipes.isRefreshing = false
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(activity,""+e.message,Toast.LENGTH_LONG).show()

                    }

                    override fun onNext(response: String) {
                        val json = JSONObject(response)
                        val jsonarr = json.getJSONArray("data")

                        for (i in 0..jsonarr.length() - 1) {

                            val jsonobj = jsonarr.getJSONObject(i)

                            val name = jsonobj.getString("name")
                            val links = jsonobj.getString("code")
                            val images = jsonobj.getString("images")

                            nuelist!!.add(NewModel(name,links,images))

                        }


                    }
                })

    }







}




