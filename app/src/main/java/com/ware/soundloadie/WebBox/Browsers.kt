package com.ware.soundloadie.WebBox

import android.app.DownloadManager
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
import android.app.Notification
import android.app.NotificationManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.NotificationCompat
import android.util.Base64
import android.webkit.*
import com.esafirm.rxdownloader.RxDownloader
import com.ware.soundloadie.R
import im.delight.android.webview.AdvancedWebView
import kotlinx.android.synthetic.main.browser.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream



class Browsers : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener, AdvancedWebView.Listener {
    var notificationManager:NotificationManager? = null
    var Primaryresponse: Response? = null
    var keyid = ""
    var app_version = "1470648201"
    var topic = ""
    var track = ""
     var strl: String? = null
     var granted: Boolean = false
     var musicLink: String? = null
     var songName: String? = null
    var fontal: String? = null
    var file_url = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.browser)

        keyid = getString(R.string.cloudID)
        swipe_views.setOnRefreshListener(this)
        swipe_views.setColorSchemeColors(Color.GRAY, Color.GREEN, Color.BLUE,
                Color.RED, Color.CYAN)
        swipe_views.setDistanceToTriggerSync(20)// in dips
        swipe_views.setSize(SwipeRefreshLayout.DEFAULT)// LARGE also can be used


        // Prepare an Interstitial Ad Listener




        webbys.setListener(this@Browsers,this)

        val intentw = intent
        musicLink = intentw.getStringExtra("url")
        fontal = musicLink!!.replace("m.soundcloud.com", "soundcloud.com").replace(" ", "")


        val webSettings = webbys.settings
        webSettings.javaScriptEnabled = true
        webSettings.allowUniversalAccessFromFileURLs = true
        webSettings.domStorageEnabled = true


        // add progress bar
       webbys.setWebChromeClient(WebChromeClient())
        webbys.loadUrl(musicLink)
        webbys.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

                view.loadUrl(url)


                view.addJavascriptInterface(object : Any() {
                    @JavascriptInterface
                    @Throws(Exception::class)
                    fun performClick(butt: String) {


                        val saveUrl = Soundclouds(butt)
                        mrSave(saveUrl)

                    }
                }, "BtnLogin")// identify which button you click
                return true
            }


        }


    }

    override fun onRefresh() {
        webbys.loadUrl(musicLink)
9
    }

    override fun onPageStarted(url: String, favicon: Bitmap?) {
        swipe_views.isRefreshing = true

    }

    override fun onPageFinished(url: String) {
        swipe_views.isRefreshing = false
        injectCSS("css/style.css")
        injectScriptFile("js/jquery.js")
        injectScriptFile("js/script.js")

        webbys.evaluateJavascript("test()") { }


    }

    override fun onPageError(errorCode: Int, description: String, failingUrl: String) {

    }

    override fun onDownloadRequested(url: String, suggestedFilename: String, mimeType: String, contentLength: Long, contentDisposition: String, userAgent: String) {

    }

    override fun onExternalPageRequest(url: String) {

    }




    private fun injectScriptFile( scriptFile: String) {
        val input: InputStream
        try {
            input = assets.open(scriptFile)
            val buffer = ByteArray(input.available())
            input.read(buffer)
            input.close()

            // String-ify the script byte-array using BASE64 encoding !!!
            val encoded = Base64.encodeToString(buffer, Base64.NO_WRAP)

            webbys.evaluateJavascript("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var script = document.createElement('script');" +
                    "script.type = 'text/javascript';" +
                    // Tell the browser to BASE64-decode the string into your script !!!
                    "script.innerHTML = decodeURIComponent(escape(window.atob('" + encoded + "')));" +
                    "parent.appendChild(script)" +
                    "})()") { }


        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

    }


    // Inject css


    private fun injectCSS(filespace: String) {
        try {
            val inputStream = assets.open(filespace)
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()
            val encoded = Base64.encodeToString(buffer, Base64.NO_WRAP)
            webbys.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    // Tell the browser to BASE64-decode the string into your script !!!
                    "style.innerHTML = decodeURIComponent(escape(window.atob('" + encoded + "')));" +
                    "parent.appendChild(style)" +
                    "})()")
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    override fun onBackPressed() {
        webbys.destroy()
        super.onBackPressed()
    }

    public override fun onResume() {
        super.onResume()
       webbys.onResume()
        // ...
    }

    public override fun onPause() {
        webbys.onPause()
        // ...
        super.onPause()
    }

    public override fun onDestroy() {
       webbys.onDestroy()
        // ...
        super.onDestroy()
    }





    fun Soundclouds(online:String):String{

        val boko = "https://api.soundcloud.com/resolve.json?url=$online&client_id=$keyid"


        try {


            val results = PrimaryServer(boko)


            var responeJson: JSONObject? = null

            try {
                responeJson = JSONObject(results)

                topic = responeJson.getString("title")
                track = responeJson.getString("id")


            } catch (e: JSONException) {
                e.printStackTrace()

            }

            val uv = "https://api.soundcloud.com/i1/tracks/$track/streams?client_id=$keyid&version=$app_version"
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



    internal fun PrimaryServer(url: String): String {
        val client = OkHttpClient()
        val requests = Request.Builder()
                .url(url)
                .build()
        Primaryresponse = client.newCall(requests).execute()

        return Primaryresponse!!.body()!!.string()
    }


    fun Alariwo(){

        val notificationBuilder =  NotificationCompat.Builder(applicationContext, "M_CH_ID")

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(getString(R.string.wait))
                .setProgress(0, 0, true)

        notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager!!.notify(1011, notificationBuilder.build());
    }



    // savers



    fun mrSave(urld: String){

        val rxDownloader = RxDownloader(this@Browsers)


           val desc = getString(R.string.downloadAudio)
           val open = getString(R.string.fab_view_audio)




        val timeStamp = System.currentTimeMillis().toString()
        val extension = "mp3"
        val filename = "soundcloud_"+timeStamp
        val name = filename + "." + extension
        val dex = File(Environment.getExternalStorageDirectory(), "soundloadie")
        if (!dex.exists())
            dex.mkdirs()

        val Download_Uri = Uri.parse(urld)
        val downloadManager =  getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request =  DownloadManager.Request(Download_Uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false)
        request.setTitle(name)
        request.setDescription(desc)
        request.setVisibleInDownloadsUi(true)
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/soundloadie/" + name)
        rxDownloader.download(request)
    }

}
