package com.example.tomatology

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerAdapter (private var titles: List<String>, private var date: List<String>, private var information: Information):
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>(){
      inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            val itemTitle: TextView = itemView.findViewById(R.id.tv_item_title)
            val itemDetail: TextView = itemView.findViewById(R.id.tv_item_description)

          init{
              itemView.setOnClickListener { v: View ->
                  // val position: Int = adapterPosition
                  val context: Context = v.context
                  val intent = Intent(context, DetailsActivity::class.java)
                  intent.putExtra("information", information)
                  // intent.putExtra("name", imageName[position])
                  context.startActivity(intent)
              }
          }
      }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_layout,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemTitle.text = titles[position]
        holder.itemDetail.text = date[position]
    }

    override fun getItemCount(): Int {
        return titles.size
    }
}