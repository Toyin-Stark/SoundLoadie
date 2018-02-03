package com.ware.soundloadie.UniversalModel

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import com.bumptech.glide.Glide
import com.ware.soundloadie.AudioBox.Audio
import com.ware.soundloadie.Containers.Search
import com.ware.soundloadie.Dbase.Playlist
import com.ware.soundloadie.R
import com.ware.soundloadie.Utils.Webpost
import com.ware.soundloadie.Utils.addPlaylist
import com.ware.soundloadie.Utils.playAudio
import com.ware.soundloadie.activities.FindView
import io.objectbox.Box
import kotlinx.android.synthetic.main.music_row_b.view.*

data class FinderModel2 (var title:String,var author:String,var link:String,var image:String,var id:String)

class FinderPlugAdapter2(var context: Context, var arraylists: ArrayList<FinderModel2>, recyclerView: RecyclerView, var finder : FindView, var audioList:ArrayList<Audio>, var playlist: Box<Playlist>, var views: View) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        var v = LayoutInflater.from(context).inflate(R.layout.music_row_b, parent, false)
        return Item(v)
    }

    override fun getItemCount(): Int {
        return arraylists.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as Item).bindData(arraylists[position])




        holder.itemView.lanas.setOnClickListener {

            playAudio(position,context,audioList)

        }

        holder.itemView.hearty.setOnClickListener {

            addPlaylist(position,context,audioList,playlist,views)

        }


        holder.itemView.menu_more.setOnClickListener {

           finder.shareMatrix(position,audioList)

        }

    }

    class Item(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindData(_data: FinderModel2) {
            itemView.titles.text = _data.title
            itemView.titles.contentDescription = _data.title

            itemView.author.text = _data.author
            itemView.author.contentDescription = _data.author

            itemView.link.text = _data.link
            itemView.link.contentDescription = "music link"

            Glide.with(itemView.context).load(_data.image).into(itemView.cover)
        }
    }



    private fun showPopup(views:View,position:Int,music: Search) {
        val menuItemView = views.findViewById<View>(R.id.menu_more)
        val popup = PopupMenu(context, menuItemView)
        val inflate = popup.menuInflater
        inflate.inflate(R.menu.popup_menu, popup.menu)

        popup.setOnMenuItemClickListener(object:PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {

                when(item!!.itemId)
                {
                    R.id.add -> {


                        return true

                    }

                    R.id.open -> {


                        return true

                    }

                    R.id.share ->{


                        return true
                    }

                }

                return false
            }


        })
        popup.show()



    }



}