package com.ware.soundloadie.UniversalModel

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.ware.soundloadie.Containers.Charts
import com.ware.soundloadie.R
import com.ware.soundloadie.activities.ChartView
import kotlinx.android.synthetic.main.genre_row.view.*

data class NewModel(val title:String, val desc:String,val imagelink:String)
class NewAdapter(var c: Context, var lists: ArrayList<NewModel>, var popular: Charts) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        var v = LayoutInflater.from(c).inflate(R.layout.genre_row, parent, false)
        return Item(v)
    }

    override fun getItemCount(): Int {
        return lists.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as Item).bindData(lists[position],popular)
    }

    class Item(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindData(_list: NewModel, popular: Charts) {

            Glide.with(itemView.context).load(_list.imagelink).into(itemView.coverImage)




            itemView.title.text = _list.title


            itemView.setOnClickListener {

                val qtent = Intent(itemView.context, ChartView::class.java)
                qtent.putExtra("name",_list.title)
                qtent.putExtra("lock",_list.desc)
                qtent.putExtra("type","trending")
                qtent.putExtra("image",_list.imagelink)
                itemView.context.startActivity(qtent)


            }
        }
    }
}