package com.example.devandroidng.infra.endpoints

import com.example.devandroidng.models.Result
import retrofit2.http.GET

interface ResultEndPoint {

    @GET("pokemon")
    fun getPokemonResults() : retrofit2.Call<Result>

    @GET("type")
    fun getTypeResults() : retrofit2.Call<Result>
}