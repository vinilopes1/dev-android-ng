package com.example.devandroidng.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.devandroidng.R
import com.example.devandroidng.models.Pokemon
import com.squareup.picasso.Picasso

internal class PokedexAdapter(val context: Context, private var pokedexList: MutableList<Pokemon>, val mItemClickListener: ItemClickListener) :
    RecyclerView.Adapter<PokedexAdapter.MyViewHolder>() {

    interface ItemClickListener{
        fun onItemClick(position: Int)
        fun onLongClick(position: Int)
    }

    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.tv_type)
        var image: ImageView = view.findViewById(R.id.image_type)

        init {
            view.setOnClickListener{
                mItemClickListener.onItemClick(adapterPosition)
            }
            view.setOnLongClickListener{
                mItemClickListener.onLongClick(adapterPosition)
                return@setOnLongClickListener true
            }
        }
    }
    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rv_types, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val pokemon = pokedexList[position]
        val str = pokemon.name
        holder.title.text = str[0].toUpperCase()+str.substring(1)
        Picasso.get()
            .load(pokemon.images.avatar_image)
            .into(holder.image)
    }
    override fun getItemCount(): Int {
        return pokedexList.size
    }
}