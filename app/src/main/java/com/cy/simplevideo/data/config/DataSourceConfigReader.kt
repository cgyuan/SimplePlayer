 package com.cy.simplevideo.data.config

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

object DataSourceConfigReader {
    private const val TAG = "DataSourceConfigReader"
    private const val CONFIG_FILE_NAME = "data_sources.json"
    
    fun getDataSources(context: Context): List<DataSourceConfig> {
        try {
            val jsonString = context.assets.open(CONFIG_FILE_NAME).bufferedReader().use { it.readText() }
            println(jsonString)
            val listType = object : TypeToken<List<DataSourceConfig>>() {}.type
            return Gson().fromJson(jsonString, listType)
        } catch (e: IOException) {
            Log.e(TAG, "Error reading data sources configuration", e)
            return emptyList()
        }
    }
    
    fun getActiveDataSource(context: Context): DataSourceConfig? {
        return getDataSources(context).firstOrNull { it.active }
    }
}