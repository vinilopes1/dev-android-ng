package com.example.devandroidng.models

import com.google.gson.annotations.SerializedName

data class Pokemon(
    @SerializedName("id")
    var id: Int,
    @SerializedName("name")
    var name: String,
    @SerializedName("sprites")
    var images: Avatar,
    @SerializedName("types")
    var types: List<Types>,
    @SerializedName("weight")
    var weight: Int,
    @SerializedName("height")
    var height: Int,
    @SerializedName("base_experience")
    var experience: Int
//    @SerializedName("thumbnailImage")
//    var image: String,
//    @SerializedName("type")
//    var types: List<String>

)