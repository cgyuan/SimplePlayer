package com.cy.simplevideo.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cy.simplevideo.data.config.DataSourceConfig
import com.cy.simplevideo.data.model.VideoDetail
import com.cy.simplevideo.data.model.VideoItem
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class RealDataSourceTest {

    private lateinit var context: Context
    private lateinit var repository: VideoRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = VideoRepository(context)
    }

    @Test
    fun testSearchVideosWithRealSource() = runBlocking {
        // 获取当前活动的数据源
        val activeSource = repository.activeDataSource.value
        assertNotNull(activeSource, "No active data source found")

        // 执行搜索
        val results = repository.searchVideos("大道朝天")
        
        // 验证结果
        assertTrue(results.isNotEmpty(), "Search results should not be empty")
        
        // 打印搜索结果
        println("Found ${results.size} videos:")
        results.forEach { video ->
            println("Title: ${video.title}")
            println("Category: ${video.category}")
            println("Update Time: ${video.updateTime}")
            println("URL: ${video.url}")
            println("---")
        }
    }

    @Test
    fun testGetVideoDetailWithRealSource() = runBlocking {
        // 先搜索一个视频
        val searchResults = repository.searchVideos("大道朝天")
        assertTrue(searchResults.isNotEmpty(), "Search results should not be empty")

        // 获取第一个视频的详情
        val firstVideo = searchResults[0]
        val detail = repository.getVideoDetail(firstVideo.url)
        
        // 验证详情
        assertNotNull(detail, "Video detail should not be null")
        
        // 打印视频详情
        println("Video Detail:")
        println("Title: ${detail.title}")
        println("Cover URL: ${detail.coverUrl}")
        println("Description: ${detail.description}")
        println("Episodes: ${detail.episodes.size}")
        detail.episodes.forEach { episode ->
            println("Episode ${episode.number}: ${episode.url}")
        }
    }

    @Test
    fun testAllDataSources() = runBlocking {
        // 获取所有数据源
        val sources = repository.dataSources.value
        assertTrue(sources.isNotEmpty(), "No data sources found")

        // 测试每个数据源
        sources.forEachIndexed { index, source ->
            println("\nTesting data source: ${source.remark}")
            println("URL: ${source.url}")
            
            // 切换到当前数据源
            repository.setActiveDataSource(index)
            
            // 执行搜索
            val results = repository.searchVideos("大道朝天")
            println("Found ${results.size} videos")
            
            if (results.isNotEmpty()) {
                // 获取第一个视频的详情
                val detail = repository.getVideoDetail(results[0].url)
                println("First video title: ${detail?.title}")
                println("Episodes count: ${detail?.episodes?.size}")
            }
        }
    }

    @Test
    fun testDataSourceSwitching() = runBlocking {
        val sources = repository.dataSources.value
        assertTrue(sources.size > 1, "Need at least 2 data sources for switching test")

        // 记录初始活动数据源
        val initialSource = repository.activeDataSource.value
        println("Initial active source: ${initialSource?.remark}")

        // 切换到第二个数据源
        repository.setActiveDataSource(1)
        val secondSource = repository.activeDataSource.value
        println("After switching to second source: ${secondSource?.remark}")

        // 验证数据源已更改
        assertTrue(secondSource != initialSource, "Active source should have changed")
        
        // 测试新数据源
        val results = repository.searchVideos("测试")
        println("Found ${results.size} videos with second source")
    }
} 