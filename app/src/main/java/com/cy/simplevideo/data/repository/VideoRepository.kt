package com.cy.simplevideo.data.repository

import android.content.Context
import android.util.Log
import com.cy.simplevideo.data.config.DataSourceConfig
import com.cy.simplevideo.data.config.DataSourceManager
import com.cy.simplevideo.data.model.VideoDetail
import com.cy.simplevideo.data.model.VideoItem
import com.cy.simplevideo.data.parser.ConfigurableHtmlParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class VideoRepository(private val context: Context) {
    
    private val TAG = "VideoRepository"
    private val dataSourceManager = DataSourceManager(context)
    
    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }
    
    val activeDataSource: StateFlow<DataSourceConfig?> = dataSourceManager.activeDataSource
    val dataSources: StateFlow<List<DataSourceConfig>> = dataSourceManager.dataSources
    
    suspend fun searchVideos(keyword: String): List<VideoItem> = withContext(Dispatchers.IO) {
        val config = dataSourceManager.activeDataSource.value ?: return@withContext emptyList()
        val parser = ConfigurableHtmlParser(config)
        
        try {
            val encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.toString())
            val url = config.url + encodedKeyword
            
            val responseBody = if (config.isPost) {
                // POST request
                val request = Request.Builder()
                    .url(url)
                    .post("".toRequestBody())
                    .build()
                
                client.newCall(request).execute().body?.string()
            } else {
                // GET request
                val request = Request.Builder()
                    .url(url)
                    .build()
                
                client.newCall(request).execute().body?.string()
            }
            
            responseBody?.let { parser.parseSearchResults(it) } ?: emptyList()
        } catch (e: IOException) {
            Log.e(TAG, "Error searching videos", e)
            emptyList()
        }
    }
    
    suspend fun getVideoDetail(url: String): VideoDetail? = withContext(Dispatchers.IO) {
        val config = dataSourceManager.activeDataSource.value ?: return@withContext null
        val parser = ConfigurableHtmlParser(config)
        
        try {
            val request = Request.Builder()
                .url(url)
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            responseBody?.let { parser.parseDetail(it) }
        } catch (e: IOException) {
            Log.e(TAG, "Error fetching video detail", e)
            null
        }
    }
    
    fun setActiveDataSource(index: Int) {
        dataSourceManager.setActiveDataSource(index)
    }
    
    fun refreshDataSources() {
        dataSourceManager.refreshDataSources()
    }
}