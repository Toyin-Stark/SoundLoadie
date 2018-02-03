package com.ware.soundloadie.AudioBox

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager



class NotificationDismissedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.extras.getInt("com.my.app.notificationId")
        if (notificationId == 101) {
            context.stopService(Intent(context, MediaPlayerService::class.java))
            sendMessage(context,"Destroy")

        }



    }





    private fun sendMessage(context:Context,baba: String) {
        // The string "my-integer" will be used to filer the intent
        val intent = Intent("message")
        // Adding some data
        intent.putExtra("messages", baba)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

}
