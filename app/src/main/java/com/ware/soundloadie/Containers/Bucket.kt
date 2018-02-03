package com.ware.soundloadie.Containers


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ware.soundloadie.R
import android.text.format.Formatter
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.bucket.*
import kotlinx.android.synthetic.main.bucket.view.*


import kotlinx.android.synthetic.main.save_row.view.*
import rebus.permissionutils.*

import java.io.File
import java.util.*


class Bucket : Fragment(),SwipeRefreshLayout.OnRefreshListener {


    private var hello = ""
    private var recycler_view:RecyclerView? = null
    private var swipe_view:SwipeRefreshLayout? = null
    private var observable:  Observable<String>? = null

    private var adapter: RecyclerView_Adapter? = null
    private var arrayList: ArrayList<Data_Model>? = null
    private var dollar: Int = 0
    private var path: String? = null
   var cur: Cursor? = null
    var permissionEnum: PermissionEnum? = null
    var granted: Boolean = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val v = inflater!!.inflate(R.layout.bucket, container, false)

        swipe_view = v.swipe_view
        recycler_view = v.recyclerview

        swipe_view!!.setOnRefreshListener(this)
        swipe_view!!.setColorSchemeColors(Color.GRAY, Color.GREEN, Color.BLUE,
                Color.RED, Color.CYAN)
        swipe_view!!.setDistanceToTriggerSync(20)// in dips
        swipe_view!!.setSize(SwipeRefreshLayout.DEFAULT)// LARGE also can be used



        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())



        recycler_view!!.setHasFixedSize(true)
        recycler_view!!.isNestedScrollingEnabled = true
        recycler_view!!.layoutManager = LinearLayoutManager(activity,LinearLayoutManager.VERTICAL,false)


        permissionEnum = PermissionEnum.WRITE_EXTERNAL_STORAGE
        granted = PermissionUtils.isGranted(activity, PermissionEnum.WRITE_EXTERNAL_STORAGE)

        if (granted) {


            FileQuery()

        } else {


            AskPermission()


        }


        return v



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



    override fun onRefresh() {

        if (granted) {


            FileQuery()

        } else {


            AskPermission()


        }

    }



    fun FileQuery() {
        swipe_view!!.isRefreshing = true
        arrayList = ArrayList<Data_Model>()
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/soundloadie/"
        val dex = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath, "soundloadie")
        if (!dex.exists())
            dex.mkdirs()


        observable = Observable.create(object: ObservableOnSubscribe<String> {
            override fun subscribe(subscriber: ObservableEmitter<String>) {
                val projection = arrayOf(MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DISPLAY_NAME, MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.MIME_TYPE, MediaStore.Files.FileColumns.DATE_ADDED, MediaStore.Files.FileColumns.MEDIA_TYPE, MediaStore.Files.FileColumns.SIZE, MediaStore.Video.Thumbnails.DATA)


                val queryUri = MediaStore.Files.getContentUri("external")


                cur = activity.contentResolver.query(queryUri, projection, MediaStore.Files.FileColumns.DATA + " LIKE ? AND " + MediaStore.Files.FileColumns.DATA + " NOT LIKE ?", arrayOf(path + "%", path + "%/%"), MediaStore.Files.FileColumns.DATE_ADDED + " DESC")

                var data: String
                var name: String
                var mime: String?
                var id: String
                var type: String
                var time: String
                var url: String
                var size: String

                if (cur != null) {

                    if (cur!!.moveToFirst()) {

                        val dataColumn = cur!!.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                        val nameColumn = cur!!.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                        val mimeColumn = cur!!.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
                        val idColumn = cur!!.getColumnIndex(MediaStore.Files.FileColumns._ID)
                        val timeColumn = cur!!.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)
                        val typeColumn = cur!!.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)
                        val sizeColumn = cur!!.getColumnIndex(MediaStore.Files.FileColumns.SIZE)


                        do {

                            data = cur!!.getString(dataColumn)
                            name = cur!!.getString(nameColumn)
                            mime = cur!!.getString(mimeColumn)
                            id = cur!!.getString(idColumn)
                            time = cur!!.getString(timeColumn)
                            type = cur!!.getString(typeColumn)
                            size = cur!!.getString(sizeColumn)
                            val date = Date(cur!!.getLong(cur!!.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)) * 1000)

                            val big = java.lang.Long.parseLong(size)
                            size = Formatter.formatFileSize(activity, big)


                            url = data


                            val milliSeconds = date.time


                            arrayList!!.add(Data_Model(name.replace(".mp3", ""), size, url, milliSeconds, data))


                        } while (cur!!.moveToNext())


                    } else {

                    }
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

                        adapter = RecyclerView_Adapter(activity, arrayList!!, this@Bucket)

                        if (adapter == null) {

                            Toast.makeText(activity, "null", Toast.LENGTH_LONG).show()

                        } else {
                            swipe_view!!.isRefreshing = false

                            recycler_view!!.adapter = adapter// set adapter on recyclerview
                            adapter!!.notifyDataSetChanged()


                        }

                    }

                    override fun onError(e: Throwable) {

                        Toast.makeText(activity, ""+e.message, Toast.LENGTH_LONG).show()


                    }

                    override fun onNext(response: String) {


                    }
                })


    }



    fun AskPermission() {
        PermissionManager.Builder()
                .permission(PermissionEnum.WRITE_EXTERNAL_STORAGE,PermissionEnum.READ_EXTERNAL_STORAGE)
                .askAgain(true)
                .askAgainCallback(object : AskAgainCallback {
                    override fun showRequestPermission(response: AskAgainCallback.UserResponse?) {

                        showDialog()
                    }


                }).callback(object : FullCallback {
            override fun result(permissionsGranted: ArrayList<PermissionEnum>?, permissionsDenied: ArrayList<PermissionEnum>?, permissionsDeniedForever: ArrayList<PermissionEnum>?, permissionsAsked: ArrayList<PermissionEnum>?) {

            }


        }).ask(this)

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        PermissionManager.handleResult(this, requestCode, permissions, grantResults);

        if (granted) {


            FileQuery()

        }
    }


    fun showDialog() {
        AlertDialog.Builder(activity)
                .setTitle(R.string.permissionTitle)
                .setMessage(R.string.permissionMessage)
                .setPositiveButton(R.string.permissionPositive, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {

                    }


                })

                .setNegativeButton(R.string.permissionPositive, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                    }


                })
                .setCancelable(false)
                .show()

    }











    fun snackUp(message:String)
    {
        val snacks = Snackbar.make(constantine!!, message, Snackbar.LENGTH_LONG)
        snacks.view.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorGreen))
        snacks.show()
    }




}

data class Data_Model(var title:String,var desc:String,var image:String,var time:Long,var link:String)

class RecyclerView_Adapter(var context: Context, var arraylists: ArrayList<Data_Model>, var downloads: Bucket) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        var v = LayoutInflater.from(context).inflate(R.layout.save_row, parent, false)
        return Item(v)
    }

    override fun getItemCount(): Int {
        return arraylists.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as Item).bindData(arraylists[position],downloads)


    }

    class Item(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindData(_data: Data_Model, _cube:Bucket) {
            itemView.title.text = _data.title
            itemView.desc.text  = _data.desc
            itemView.links.text = _data.link
            itemView.time.setReferenceTime(_data.time)

            val ext = _data.desc;










            itemView.setOnClickListener {
                val urx = _data.link
                val binta = Intent()
                binta.action = Intent.ACTION_VIEW
                val file = File(urx)
                binta.setDataAndType(Uri.fromFile(file), "audio/*")
                itemView.context.startActivity(binta)

            }
        }
    }

}
