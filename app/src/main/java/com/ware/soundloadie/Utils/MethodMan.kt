package com.ware.soundloadie.Utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import com.ware.soundloadie.AudioBox.Audio
import com.ware.soundloadie.AudioBox.MediaPlayerService
import com.ware.soundloadie.AudioBox.Player
import com.ware.soundloadie.AudioBox.StorageUtil
import com.ware.soundloadie.Containers.Charts
import com.ware.soundloadie.Dbase.Playlist
import com.ware.soundloadie.Dbase.PlaylistSquare
import com.ware.soundloadie.Dbase.VPlaylist
import com.ware.soundloadie.R
import com.ware.soundloadie.UniversalModel.PlayFinderModel
import com.ware.soundloadie.UniversalModel.VideoPlayModel
import com.ware.soundloadie.UniversalModel.VideoPlayPlugAdapter
import com.ware.soundloadie.WebBox.Browsers
import com.ware.soundloadie.activities.PlaylistView
import com.ware.soundloadie.activities.SoundPlayLister
import io.objectbox.Box
import io.objectbox.query.Query
import kotlinx.android.synthetic.main.chart_view.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


//play any selected song
fun playAudio(audioIndex: Int, context: Context, audioList:ArrayList<Audio>) {

    if (!isMyServiceRunning(MediaPlayerService::class.java,context)) {
        //Store Serializable audioList to SharedPreferences
        val storage = StorageUtil(context)
        storage.storeAudio(audioList)
        storage.storeAudioIndex(audioIndex)

        val playerIntent = Intent(context, MediaPlayerService::class.java)
        ContextCompat.startForegroundService(context,playerIntent)

        val broadcastIntent = Intent("message")
        broadcastIntent.putExtra("messages","showplayer")
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent)

    } else {
        //Store the new audioIndex to SharedPreferences
        val storage = StorageUtil(context)
        storage.storeAudio(audioList)
        storage.storeAudioIndex(audioIndex)

        val broadcast = Intent("message")
        broadcast.putExtra("messages","showplayer")
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast)

        val broadcastIntent = Intent(Charts.Broadcast_PLAY_NEW_AUDIO)
        broadcastIntent.putExtra("messages", "nue"+audioIndex)

        context.sendBroadcast(broadcastIntent)

    }


}


// Check if a service is running
fun isMyServiceRunning(serviceClass: Class<*>,context: Context): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}







// Add to Audio Favourite Playlist
fun addPlaylist(audioIndex: Int,context: Context,audioList:ArrayList<Audio>,playlist: Box<Playlist>,view:View){
    var activeAudio: Audio? = null
    val storage = StorageUtil(context)
    storage.storeAudio(audioList)

    if (audioIndex != -1 && audioIndex < audioList!!.size) {
        //index is in a valid range
        activeAudio = audioList!![audioIndex]
    }
    val player = activeAudio!!.data
    val titles = activeAudio.title
    val genre  = activeAudio.album
    val author = activeAudio.artist
    val art    = activeAudio.art
    val link   = activeAudio!!.link

    val play = Playlist(player = player,titles = titles,genre = genre,author = author,art = art,link = link)
    playlist!!.put(play)
    snackUp(context.getString(R.string.added),view,context)

}



// Add to soundcloud playlist Favourites
fun addPlaylistSquare(position: Int, context: Context, arrayplay:ArrayList<PlayFinderModel>,playlistSquare: Box<PlaylistSquare>, view:View,mine:String){

    val titles =  arrayplay[position].title
    val author  = arrayplay[position].author
    val link =    arrayplay[position].link
    val art    =  arrayplay[position].image
    val id   =    arrayplay[position].id
    val track   = arrayplay[position].count

    val play = PlaylistSquare(titles = titles,author = author,link = link,art = art,idx = id,track = track,mime = mine)
     playlistSquare.put(play)
    snackUp(context.getString(R.string.added),view,context)

}


// View Playlist




//Open link in SoundCloud web version

fun Webpost(audioIndex: Int,context: Context,audioList: ArrayList<Audio>)
{


    var activeAudio: Audio? = null
    val storage = StorageUtil(context)
    storage.storeAudio(audioList)

    if (audioIndex != -1 && audioIndex < audioList!!.size) {
        //index is in a valid range
        activeAudio = audioList[audioIndex]
    }
    val intent = Intent(context, Browsers::class.java)
    intent.putExtra("url",activeAudio!!.link)
    intent.putExtra("name",activeAudio!!.title)
    context.startActivity(intent)

}


//Share music link with Soundloadie



fun snackUp(message:String,view:View,context: Context)
{


    val snacks = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
    snacks.view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorGreen))
    snacks.show()
}




// Play Previous Track
fun previous(context: Context) {


    val broadcastIntent = Intent(Player.Broadcast_PLAY_PREV_AUDIO)
    context.sendBroadcast(broadcastIntent)

}


// Play next track
fun next(context: Context) {


    val broadcastIntent = Intent(Player.Broadcast_PLAY_NEXT_AUDIO)
    context.sendBroadcast(broadcastIntent)

}



@Throws(IOException::class)
fun readFile(fileName: String,context: Context): String {
    var reader: BufferedReader? = null
    reader = BufferedReader(InputStreamReader(context.assets.open(fileName), "UTF-8"))

    var content = ""
    while (true) {
        var line: String? = reader.readLine() ?: break
        content += line

    }

    return content
}


// Show mini player

fun discoveryChannel(context: Context,name: String,query:String,image: String){

    val intent = Intent(context, SoundPlayLister::class.java)
    intent.putExtra("name",name)
    intent.putExtra("query",query)
    intent.putExtra("image",image)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.applicationContext.startActivity(intent)
}




fun videoremover(videoIndex: Int, videoplaylist: Box<VPlaylist>, vlist:ArrayList<VideoPlayModel>, videoplaylistQuery: Query<VPlaylist>,videowave: VideoPlayPlugAdapter){
    val playnote = videoplaylistQuery!!.find()
    val gone  = playnote[videoIndex]
    videoplaylist!!.remove(gone)
    vlist!!.removeAt(videoIndex)
    videowave!!.notifyDataSetChanged()

}





