package com.example.devandroidng.models

import com.google.gson.annotations.SerializedName

data class Result(
    @SerializedName("results")
    var results : List<ItemResult>

)