 package com.cy.simplevideo.data.config

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DataSourceManager(private val context: Context) {
    private val TAG = "DataSourceManager"
    private val PREFS_NAME = "data_source_prefs"
    private val ACTIVE_SOURCE_INDEX_KEY = "active_source_index"
    
    private val preferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _dataSources = MutableStateFlow<List<DataSourceConfig>>(emptyList())
    private val _activeDataSource = MutableStateFlow<DataSourceConfig?>(null)
    
    val dataSources: StateFlow<List<DataSourceConfig>> = _dataSources.asStateFlow()
    val activeDataSource: StateFlow<DataSourceConfig?> = _activeDataSource.asStateFlow()
    
    init {
        loadDataSources()
    }
    
    private fun loadDataSources() {
        try {
            val sources = DataSourceConfigReader.getDataSources(context)
            _dataSources.value = sources
            
            val activeIndex = preferences.getInt(ACTIVE_SOURCE_INDEX_KEY, -1)
            
            _activeDataSource.value = if (activeIndex >= 0 && activeIndex < sources.size) {
                sources[activeIndex]
            } else {
                sources.firstOrNull { it.active }
            }
            
            Log.d(TAG, "Loaded ${sources.size} data sources, active: ${_activeDataSource.value?.remark}")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading data sources", e)
        }
    }
    
    fun setActiveDataSource(index: Int) {
        val sources = _dataSources.value
        if (index < 0 || index >= sources.size) {
            Log.e(TAG, "Invalid data source index: $index")
            return
        }
        
        _activeDataSource.value = sources[index]
        preferences.edit().putInt(ACTIVE_SOURCE_INDEX_KEY, index).apply()
        Log.d(TAG, "Set active data source to: ${sources[index].remark}")
    }
    
    fun refreshDataSources() {
        loadDataSources()
    }
}