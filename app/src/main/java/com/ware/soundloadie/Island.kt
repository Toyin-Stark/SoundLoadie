package com.ware.soundloadie
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.os.StrictMode
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Patterns
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.downloader.*
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.empire.*
import kotlinx.android.synthetic.main.playback.*


import org.json.JSONException
import org.json.JSONObject

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.ArrayList
import java.util.concurrent.TimeUnit


import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import rebus.permissionutils.*



class Island : AppCompatActivity() {

    var track = ""
    var digits: TextView? = null

     var pass = ""
     var file_url = ""
     var topic = ""
     var mu: Array<String>? = null


    var version = ""
    var Primaryresponse: Response? = null
    var realUrl =""


     var file: File? = null
     var save = ""
     var entrance: Animation? = null

     var tlc = ""
     var granted: Boolean = false
     var permissionEnum: PermissionEnum? = null
    var observable: Observable<String>? = null
   var app_version = "1470648201"
    var playUri:Uri? = null
    var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.empire)


        pass = getString(R.string.cloudID)
        version = "1470648201"


        mInterstitialAd = com.facebook.ads.InterstitialAd(this@Island, getString(R.string.intersistal))



        mInterstitialAd!!.setAdListener(object : InterstitialAdListener {
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




        entrance = AnimationUtils.loadAnimation(applicationContext, R.anim.entrance)

        val intents = intent
        tlc = intents.getStringExtra(Intent.EXTRA_TEXT)
        val config = PRDownloaderConfig.newBuilder()
                .setReadTimeout(30000)
                .setConnectTimeout(30000)
                .build()
        PRDownloader.initialize(applicationContext, config)
        Glide.with(applicationContext).load(R.raw.crocodile).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(cover)


        val snackbar = Snackbar.make(findViewById<View>(android.R.id.content), "Please wait...", Snackbar.LENGTH_LONG)
        val snackBarView = snackbar.view
        snackBarView.setBackgroundColor(Color.parseColor("#ff8800"))
        snackbar.show()


        permissionEnum = PermissionEnum.WRITE_EXTERNAL_STORAGE
        granted = PermissionUtils.isGranted(this@Island, PermissionEnum.WRITE_EXTERNAL_STORAGE)

        if (granted) {

            processDownload(tlc)

        } else {


            AskPermission()


        }




        playbuts.setOnClickListener {

            val intent = Intent(android.content.Intent.ACTION_VIEW)
            intent.setDataAndType(playUri, "audio/mp3")
            startActivity(intent);
        }


        closers.setOnClickListener {

            finish()
        }

        raters.setOnClickListener {

            Rated()
        }


    }







    private fun showDialog(response: AskAgainCallback.UserResponse) {
        AlertDialog.Builder(this@Island)
                .setTitle("Permission needed")
                .setMessage("This app really need to use this permission, you wont to authorize it?")
                .setPositiveButton("OK") { dialogInterface, i -> response.result(true) }
                .setNegativeButton("NOT NOW") { dialogInterface, i -> response.result(false) }
                .show()
    }





    fun snackUp(message:String)
    {
        val snacks = Snackbar.make(constantine!!, message, Snackbar.LENGTH_LONG)
        snacks.view.setBackgroundColor(ContextCompat.getColor(this@Island, R.color.colorGreen))
        snacks.show()
    }






    fun Rated() {

        Toast.makeText(applicationContext, "Please rate this app", Toast.LENGTH_LONG).show()

        val uri = Uri.parse("market://details?id=" + packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)))
        }

    }




    fun processDownload(urlBatch:String)
    {

        observable = Observable.create(object: ObservableOnSubscribe<String> {
            override fun subscribe(subscriber: ObservableEmitter<String>) {

                if (urlBatch.contains("soundcloud.com")) {

                    mu = extracTors(urlBatch)
                    realUrl = mu!![0]
                   val downloadUrl = Soundclouds(realUrl)

                    subscriber.onNext(downloadUrl)
                    subscriber.onComplete()
                }else{

                    finish()
                }

            }
        })

        observable!!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: Observer<String> {
                    override fun onSubscribe(d: Disposable) {


                    }

                    override fun onComplete() {


                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(applicationContext,""+e.message,Toast.LENGTH_LONG).show()

                    }

                    override fun onNext(response: String) {

                        musicDownloader(response)


                    }
                })

    }








    fun musicDownloader(saveUrl:String){
        Glide.with(applicationContext).load(R.raw.drummer).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(cover)
        constantine.setBackgroundColor(Color.parseColor("#FEC70F"))
        progressBar.isIndeterminate = false
        val dex = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath, "soundloadie")
        if (!dex.exists())
            dex.mkdirs()
        val timeStamp =  System.currentTimeMillis();
        val name = topic + ".mp3"
        val fileplace = File(dex,name)
        val downloadId = PRDownloader.download(saveUrl,dex.absolutePath, name)
        .build()
                .setOnProgressListener(object :OnProgressListener{
                    override fun onProgress(progress: Progress?) {
                        val progressPercent = progress!!.currentBytes * 100 / progress.totalBytes

                        val pInt = progressPercent.toInt()
                        progressBar.progress = progressPercent.toInt()
                        status.text = "$pInt %"
                    }


                }).start(object:OnDownloadListener{
            override fun onError(error: Error?) {


            }

            override fun onDownloadComplete() {

                val paths = fileplace.absolutePath
                MediaScannerConnection.scanFile(applicationContext, arrayOf(paths), null) { p0, uri ->
                    playUri = uri

                }
                doneImage.visibility = View.VISIBLE
                cover.visibility = View.GONE
                playbox.visibility = View.VISIBLE

                progressBar.visibility = View.GONE
                status.text = topic
                cover.adjustViewBounds = false



                snackUp(getString(R.string.complete))
            }


        })
    }









    fun Soundclouds(mp3Url:String):String{



        try {

            val boko = "https://api.soundcloud.com/resolve.json?url=$mp3Url&client_id=$pass"
            val results = PrimaryServer(boko)


            var responeJson: JSONObject? = null

            try {
                responeJson = JSONObject(results)

                topic = responeJson.getString("title")
                track = responeJson.getString("id")


            } catch (e: JSONException) {
                e.printStackTrace()

            }

            val uv = "https://api.soundcloud.com/i1/tracks/$track/streams?client_id=$pass&version=$app_version"
            var result: String? = null
            try {
                result = PrimaryServer(uv)

                var responeJsons: JSONObject? = null
                try {
                    responeJsons = JSONObject(result)
                    file_url = responeJsons.getString("http_mp3_128_url")


                } catch (e: JSONException) {
                    e.printStackTrace()
                }


            } catch (e: IOException) {
                e.printStackTrace()
            }


        } catch (e: IOException) {
            e.printStackTrace()

        }

        return file_url
    }




















     fun PrimaryServer(url: String): String {
        val client = OkHttpClient()
        val requests = Request.Builder()
                .url(url)
                .build()
        Primaryresponse = client.newCall(requests).execute()

        return Primaryresponse!!.body()!!.string()
    }





    companion object {


        fun extracTors(text: String): Array<String> {
            val links = ArrayList<String>()
            val m = Patterns.WEB_URL.matcher(text)
            while (m.find()) {
                val urls = m.group()
                links.add(urls)
            }

            return links.toTypedArray()
        }
    }







    fun AskPermission()
    {
        PermissionManager.Builder()
                .permission(PermissionEnum.WRITE_EXTERNAL_STORAGE)
                .askAgain(true)
                .askAgainCallback { showDialog() }
                .callback(object: FullCallback {
            override fun result(permissionsGranted: ArrayList<PermissionEnum>?, permissionsDenied: ArrayList<PermissionEnum>?, permissionsDeniedForever: ArrayList<PermissionEnum>?, permissionsAsked: ArrayList<PermissionEnum>?) {
                granted = PermissionUtils.isGranted(this@Island, PermissionEnum.WRITE_EXTERNAL_STORAGE)
                if (granted) {

                    processDownload(tlc)

                } else {


                    AskPermission()


                }

            }

        }).ask(this)

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        PermissionManager.handleResult(this, requestCode, permissions, grantResults)





    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun showDialog()
    {
        AlertDialog.Builder(this@Island)
                .setTitle(R.string.permissionTitle)
                .setMessage(R.string.permissionMessage)
                .setPositiveButton(R.string.permissionPositive,object: DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                    }


                })

                .setNegativeButton(R.string.permissionPositive,object: DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                    }


                })
                .setCancelable(false)
                .show()

    }


}
