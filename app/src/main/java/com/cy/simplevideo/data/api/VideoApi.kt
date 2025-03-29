package com.cy.simplevideo.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VideoApi {
    @GET("index.php/vod/search.html")
    suspend fun searchVideos(@Query("wd") keyword: String): String

    @GET("{url}")
    suspend fun getVideoDetail(@Path("url") url: String): String
} 