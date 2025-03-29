package com.cy.simplevideo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cy.simplevideo.data.config.DataSourceConfig
import com.cy.simplevideo.data.model.VideoDetail
import com.cy.simplevideo.data.model.VideoItem
import com.cy.simplevideo.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VideoViewModel(private val context: Context) : ViewModel() {
    private val repository = VideoRepository(context)
    
    private val _searchResults = MutableStateFlow<List<VideoItem>>(emptyList())
    val searchResults: StateFlow<List<VideoItem>> = _searchResults

    private val _videoDetail = MutableStateFlow<VideoDetail?>(null)
    val videoDetail: StateFlow<VideoDetail?> = _videoDetail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // 数据源相关状态
    val dataSources: StateFlow<List<DataSourceConfig>> = repository.dataSources
    val activeDataSource: StateFlow<DataSourceConfig?> = repository.activeDataSource

    fun searchVideos(keyword: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val results = repository.searchVideos(keyword)
                _searchResults.value = results
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getVideoDetail(url: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val detail = repository.getVideoDetail(url)
                _videoDetail.value = detail
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 数据源切换功能
    fun setActiveDataSource(index: Int) {
        repository.setActiveDataSource(index)
    }

    fun refreshDataSources() {
        repository.refreshDataSources()
    }
} 