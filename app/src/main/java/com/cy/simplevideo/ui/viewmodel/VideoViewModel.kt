package com.cy.simplevideo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cy.simplevideo.data.api.RetrofitClient
import com.cy.simplevideo.data.model.VideoDetail
import com.cy.simplevideo.data.model.VideoItem
import com.cy.simplevideo.data.parser.DetailParser
import com.cy.simplevideo.data.parser.HtmlParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VideoViewModel : ViewModel() {
    private val _searchResults = MutableStateFlow<List<VideoItem>>(emptyList())
    val searchResults: StateFlow<List<VideoItem>> = _searchResults

    private val _videoDetail = MutableStateFlow<VideoDetail?>(null)
    val videoDetail: StateFlow<VideoDetail?> = _videoDetail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun searchVideos(keyword: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val html = RetrofitClient.videoApi.searchVideos(keyword)
                val results = HtmlParser.parseSearchResults(html)
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
                
                val html = RetrofitClient.videoApi.getVideoDetail(url)
                val detail = DetailParser.parseDetail(html)
                _videoDetail.value = detail
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
} 