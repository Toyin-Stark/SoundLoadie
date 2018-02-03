package com.ware.soundloadie.Containers


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.ware.soundloadie.R
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.learner.*
import kotlinx.android.synthetic.main.learner.view.*
import kotlinx.android.synthetic.main.tut_row.view.*



class Learner : Fragment() {
    private var observable: Observable<String>? = null
    private var adapter: Tut_Adapter? = null
    private var arrayList: ArrayList<Tut_Model>? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater!!.inflate(R.layout.learner, container, false)



        v.fabe.setOnClickListener {

            callSoundCloud()
        }


        return v
    }


    override fun onStart() {
        super.onStart()

        Tutorial()

    }

    fun Tutorial() {
        arrayList = ArrayList<Tut_Model>()

        recycler_views.isNestedScrollingEnabled =false
        recycler_views.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        observable = Observable.create(object: ObservableOnSubscribe<String> {
            override fun subscribe(subscriber: ObservableEmitter<String>) {


                val listValue = arrayOf(getString(R.string.intro1),getString(R.string.intro2),getString(R.string.intro3))
                val IMAGES = arrayOf(R.drawable.a1, R.drawable.a2, R.drawable.a3)

                for (i in 0 until listValue.size) {


                    val title = listValue[i]
                    val imgs = IMAGES[i]

                    arrayList!!.add(Tut_Model(title, imgs))
                    subscriber.onNext("")


                }


                subscriber.onComplete()
            }


        })


        observable!!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: Observer<String> {
                    override fun onComplete() {
                        adapter = Tut_Adapter(activity, arrayList!!)

                        if (adapter == null) {


                        } else {

                            recycler_views.adapter = adapter// set adapter on recyclerview
                            adapter!!.notifyDataSetChanged()

                        }

                    }
                    override fun onSubscribe(d: Disposable) {

                    }


                    override fun onError(e: Throwable) {

                        Toast.makeText(activity, "" + e, Toast.LENGTH_LONG).show()


                    }

                    override fun onNext(response: String) {


                    }
                })


    }


    private fun callSoundCloud() {
        val apppackage = "com.soundcloud.android"
        val cx = activity
        try {
            val i = cx.packageManager.getLaunchIntentForPackage(apppackage)
            cx.startActivity(i)
        } catch (e: Exception) {

            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.soundcloud.android")))
        }

    }



}



data class Tut_Model(var title:String,var image:Int)

class Tut_Adapter(var context: Context, var arraylists: ArrayList<Tut_Model>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        var v = LayoutInflater.from(context).inflate(R.layout.tut_row, parent, false)
        return Item(v)
    }

    override fun getItemCount(): Int {
        return arraylists.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as Item).bindData(arraylists[position])




    }

    class Item(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindData(_data: Tut_Model) {
            itemView.title.text = _data.title
            itemView.icons.setImageResource(_data.image)

        }
    }

}

