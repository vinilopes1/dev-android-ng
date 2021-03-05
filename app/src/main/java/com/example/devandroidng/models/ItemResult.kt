package com.example.devandroidng.models

import com.google.gson.annotations.SerializedName

data class ItemResult (
        @SerializedName("name")
        var name: String,
        @SerializedName("url")
        var url: String
)