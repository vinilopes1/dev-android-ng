package com.example.devandroidng.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.devandroidng.app.PokemonDetailActivity
import com.example.devandroidng.R
import com.example.devandroidng.models.Pokemon
import com.squareup.picasso.Picasso

internal class PokemonAdapter(val context: Context, private var pokemonList: MutableList<Pokemon>, var activity: Activity) :
    RecyclerView.Adapter<PokemonAdapter.MyViewHolder>() {

    var listAllPokemons : MutableList<Pokemon> = ArrayList()

    init {
        listAllPokemons.addAll(pokemonList)
    }

    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.tv_pokemon)
        var image: ImageView = view.findViewById(R.id.image_pokemon)
    }
    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rv_pokemons, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val pokemon = pokemonList[position]
        val str = pokemonList.get(position).name
        holder.title.text = str[0].toUpperCase()+str.substring(1)
        Picasso.get()
            .load(pokemon.images.avatar_image)
            .into(holder.image)
        holder.itemView.setOnClickListener {
            onClick(pokemon)
        }
    }
    override fun getItemCount(): Int {
        return pokemonList.size
    }

    private fun onClick(pokemon: Pokemon){
        val intent = Intent(activity, PokemonDetailActivity::class.java)
        intent.putExtra("pokemonId", pokemon.id.toString())
        activity.startActivity(intent)
    }

}