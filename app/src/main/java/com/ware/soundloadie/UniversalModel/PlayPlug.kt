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
import com.ware.soundloadie.R
import kotlinx.android.synthetic.main.play_row_b.view.*


data class PlayModel (var title:String, var author:String, var link:String, var image:String)
class PlayPlugAdapter(var context: Context, var arraylists: ArrayList<PlayModel>, var music : Playpage) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        var v = LayoutInflater.from(context).inflate(R.layout.play_row_b, parent, false)
        return Item(v)
    }

    override fun getItemCount(): Int {
        return arraylists.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as Item).bindData(arraylists[position])




        holder.itemView.lanas.setOnClickListener {

            music.playAudio(position)

        }



        holder.itemView.hearty.setOnClickListener {

            music.remover(position)

        }


    }



    class Item(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindData(_data: PlayModel) {
            itemView.titles.text = _data.title
            itemView.titles.contentDescription = _data.title

            itemView.author.text = _data.author
            itemView.author.contentDescription = _data.author

            itemView.link.text   = _data.link
            itemView.link.contentDescription = "music link"

            Glide.with(itemView.context).load(_data.image).into(itemView.cover)
        }
    }








    private fun showPopup(views: View, position:Int, music:Playpage) {
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