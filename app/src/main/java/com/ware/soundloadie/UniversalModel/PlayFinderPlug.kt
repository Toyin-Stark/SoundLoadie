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
import com.ware.soundloadie.Containers.Search
import com.ware.soundloadie.Dbase.PlaylistSquare
import com.ware.soundloadie.R
import com.ware.soundloadie.Utils.*
import com.ware.soundloadie.activities.FindView
import com.ware.soundloadie.activities.SoundPlayLister
import io.objectbox.Box
import kotlinx.android.synthetic.main.play_finder_row_b.view.*

data class PlayFinderModel (var title:String,var author:String,var link:String,var image:String,var id:String,var count:String)
class PlayFinder (var context: Context, var arrayplay:ArrayList<PlayFinderModel>, var playlistSquare: Box<PlaylistSquare>, var views: View,var active:SoundPlayLister) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        var v = LayoutInflater.from(context).inflate(R.layout.play_finder_row_b, parent, false)
        return Item(v)
    }

    override fun getItemCount(): Int {
        return arrayplay.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as Item).bindData(arrayplay[position])


        val name = arrayplay[position].title
        val id =   arrayplay[position].id
        val count = arrayplay[position].count
        var image  = arrayplay[position].image
        if (image.contains(":")){

        }else{

            image = "empty"
        }


        holder.itemView.lanas.setOnClickListener {

            active.viewPlaylist(id,name,image,count)

        }

        holder.itemView.hearty.setOnClickListener {

            addPlaylistSquare(position,context,arrayplay,playlistSquare,views,"audio")

        }




    }

    class Item(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindData(_data: PlayFinderModel) {
            val songs = itemView.context.getString(R.string.songs)
            itemView.titles.text = _data.title
            itemView.titles.contentDescription = _data.title

            itemView.author.text = _data.author
            itemView.author.contentDescription = _data.author

            itemView.count.text = _data.count  +" $songs"
            itemView.count.contentDescription = _data.count +" $songs"

            itemView.link.text = _data.link
            itemView.link.contentDescription = "music link"


            if(_data.image.contains(":")){

                Glide.with(itemView.context).load(_data.image).into(itemView.cover)
            }else{

                itemView.cover.setImageResource(R.drawable.playdummy)
            }

        }
    }





}




























//=============================================================================================================================
 //                                                 PLAYFINDER 2
//=======================================================================================================================================



class PlayFinder2 (var context: Context, var arrayplay:ArrayList<PlayFinderModel>, var playlistSquare: Box<PlaylistSquare>, var views: View,var active:FindView) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        var v = LayoutInflater.from(context).inflate(R.layout.play_finder_row_b, parent, false)
        return Item(v)
    }

    override fun getItemCount(): Int {
        return arrayplay.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as Item).bindData(arrayplay[position])


        val name = arrayplay[position].title
        val id =   arrayplay[position].id
        val count = arrayplay[position].count
        var image  = arrayplay[position].image
        if (image.contains(":")){

        }else{

            image = "empty"
        }


        holder.itemView.lanas.setOnClickListener {

            active.viewPlaylist(id,name,image,count)

        }

        holder.itemView.hearty.setOnClickListener {

            addPlaylistSquare(position,context,arrayplay,playlistSquare,views,"audio")

        }




    }

    class Item(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindData(_data: PlayFinderModel) {
            val songs = itemView.context.getString(R.string.songs)
            itemView.titles.text = _data.title
            itemView.titles.contentDescription = _data.title

            itemView.author.text = _data.author
            itemView.author.contentDescription = _data.author

            itemView.count.text = _data.count  +" $songs"
            itemView.count.contentDescription = _data.count +" $songs"

            itemView.link.text = _data.link
            itemView.link.contentDescription = "music link"


            if(_data.image.contains(":")){

                Glide.with(itemView.context).load(_data.image).into(itemView.cover)
            }else{

                itemView.cover.setImageResource(R.drawable.playdummy)
            }

        }
    }





}