package com.ware.soundloadie.Containers


import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import com.ware.soundloadie.App
import com.ware.soundloadie.AudioBox.Audio
import com.ware.soundloadie.AudioBox.MediaPlayerService
import com.ware.soundloadie.AudioBox.StorageUtil
import com.ware.soundloadie.Dbase.Playlist
import com.ware.soundloadie.MainActivity
import com.ware.soundloadie.R
import com.ware.soundloadie.UniversalModel.FinderModel2
import com.ware.soundloadie.UniversalModel.FinderPlugAdapter2
import com.ware.soundloadie.WebBox.Browsers
import com.ware.soundloadie.activities.FindView
import io.objectbox.Box
import io.objectbox.query.Query
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.searchs.*

import kotlinx.android.synthetic.main.searchs.view.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

import java.io.IOException
import java.util.concurrent.TimeUnit


class Search : Fragment(),SwipeRefreshLayout.OnRefreshListener {

    var observable:  Observable<String>? = null
    var arraylistx =  ArrayList<FinderModel2>()
    var uri:String? = null
    var playlist: Box<Playlist>? = null
    var playlistQuery: Query<Playlist>? = null


    var searchView: EditText? = null
    var constantine:RelativeLayout?= null
    var keyID = ""
    var audioList  =  ArrayList<Audio>()
    var serviceBound = false

    private var player: MediaPlayerService? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val v = inflater!!.inflate(R.layout.searchs, container, false)

        playlist = (activity.application as App).boxStore.boxFor(Playlist::class.java)

        keyID = getString(R.string.cloudID)
        searchView = v.search_view
        constantine = v.constantine

        v.cardy.setOnClickListener {

            startActivity(Intent(activity,FindView::class.java))

        }


        v.search_view.setOnClickListener {

            startActivity(Intent(activity,FindView::class.java))

        }

        return v
    }

















    override fun onRefresh() {

    }






    override fun onResume() {
        super.onResume()


    }










}
