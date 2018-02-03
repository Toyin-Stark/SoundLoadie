package com.ware.soundloadie.UniversalModel

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import com.bumptech.glide.Glide
import com.ware.soundloadie.Containers.Playpage
import com.ware.soundloadie.Containers.Videos
import com.ware.soundloadie.R
import kotlinx.android.synthetic.main.play_video_row_b.view.*

data class VideoPlayModel (var title:String,  var link:String, var id:String,var published:Long)
class VideoPlayPlugAdapter(var context: Context, var arraylists: ArrayList<VideoPlayModel>, var video : Playpage) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        var v = LayoutInflater.from(context).inflate(R.layout.play_video_row_b, parent, false)
        return Item(v)
    }

    override fun getItemCount(): Int {
        return arraylists.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as Item).bindData(arraylists[position])




        holder.itemView.setOnClickListener {

            video.playVideo(position)

        }


        holder.itemView.hearty.setOnClickListener {

            video.videoremover(position)

        }






    }



    class Item(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindData(_data: VideoPlayModel) {
            itemView.titles.text = _data.title
            itemView.ids.text = _data.id

            itemView.titles.contentDescription = _data.title
            itemView.time.setReferenceTime(_data.published)

            val imageUrl = "http://img.youtube.com/vi/${_data.id}/maxresdefault.jpg"


            itemView.link.text   = _data.link
            itemView.link.contentDescription = "music link"
            Glide.with(itemView.context).load(imageUrl).into(itemView.cover)
        }
    }








    private fun showPopup(views: View, position:Int, music: Playpage) {
        val menuItemView = views.findViewById<View>(R.id.menu_more)
        val popup = PopupMenu(context, menuItemView)
        val inflate = popup.menuInflater
        inflate.inflate(R.menu.popup_menu, popup.menu)

        popup.setOnMenuItemClickListener(object: PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {

                when(item!!.itemId)
                {


                    R.id.open -> {

                        music.Webpost(position)
                        return true

                    }

                    R.id.share ->{

                        music.shareMatrix(position)

                        return true
                    }

                }

                return false
            }


        })
        popup.show()



    }




}