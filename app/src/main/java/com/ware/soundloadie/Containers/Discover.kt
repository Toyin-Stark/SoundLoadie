package com.ware.soundloadie.Containers


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.ware.soundloadie.R
import com.ware.soundloadie.UniversalModel.DiscoverAdapter
import com.ware.soundloadie.UniversalModel.DiscoverModel
import com.ware.soundloadie.Utils.readFile
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.discover.*
import org.json.JSONObject


/**
 * A simple [Fragment] subclass.
 */
class Discover : Fragment() {
    var discover: DiscoverAdapter? = null
    var discoverlist:ArrayList<DiscoverModel>? = null
    var observable: Observable<String>? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
       val v = inflater!!.inflate(R.layout.discover, container, false)

        return v
    }


    override fun onStart() {
        super.onStart()

        discoveryCharts()
    }


    fun discoveryCharts()
    {
        discoverlist = ArrayList<DiscoverModel>()
        recyclerView1.adapter = null
        recyclerView1.isNestedScrollingEnabled = true
        recyclerView1.layoutManager =  GridLayoutManager(activity,2)

        observable = Observable.create(object: ObservableOnSubscribe<String> {
            override fun subscribe(subscriber: ObservableEmitter<String>) {


                val response = readFile("data/playlist_data.json",activity)

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
                        discover = DiscoverAdapter(activity, discoverlist!!)
                        recyclerView1.adapter = discover
                        discover!!.notifyDataSetChanged()

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

                            discoverlist!!.add(DiscoverModel(name,links,images))

                        }


                    }
                })

    }




}
