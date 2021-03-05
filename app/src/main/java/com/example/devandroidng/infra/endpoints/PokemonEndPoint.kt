package com.example.devandroidng.infra.endpoints

import com.example.devandroidng.models.Pokemon
import retrofit2.http.GET
import retrofit2.http.Path

interface PokemonEndPoint {

    @GET("{url}")
    fun getPokemon(@Path("url") url: String) : retrofit2.Call<Pokemon>

    @GET("pokemon/{id}")
    fun getPokemonDetail(@Path("id") id: String) : retrofit2.Call<Pokemon>
}