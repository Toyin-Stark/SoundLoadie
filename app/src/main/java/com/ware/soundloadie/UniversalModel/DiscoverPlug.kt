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
import com.ware.soundloadie.Utils.discoveryChannel
import com.ware.soundloadie.activities.ChartView
import kotlinx.android.synthetic.main.discover_row.view.*

data class DiscoverModel(val title:String, val desc:String,val imagelink:String)
class DiscoverAdapter(var c: Context, var lists: ArrayList<DiscoverModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        var v = LayoutInflater.from(c).inflate(R.layout.discover_row, parent, false)
        return Item(v)
    }

    override fun getItemCount(): Int {
        return lists.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as Item).bindData(lists[position])
        val name = lists[position].title
        val query = lists[position].desc
        val image = lists[position].imagelink


        holder.itemView.setOnClickListener {

            discoveryChannel(holder.itemView.context,name,query,image)
        }
    }

    class Item(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindData(_list: DiscoverModel) {

            Glide.with(itemView.context).load(_list.imagelink).placeholder(R.drawable.dummy).into(itemView.coverImage)
            itemView.title.text = _list.title



        }
    }
}