package com.cy.simplevideo.data.parser

import android.util.Log
import com.cy.simplevideo.data.config.DataSourceConfig
import com.cy.simplevideo.data.model.Episode
import com.cy.simplevideo.data.model.VideoDetail
import com.cy.simplevideo.data.model.VideoItem
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class ConfigurableHtmlParser(private val config: DataSourceConfig) {
    
    private val TAG = "ConfigurableHtmlParser"
    
    fun parseSearchResults(html: String): List<VideoItem> {
        val doc: Document = Jsoup.parse(html)
        val videoList: MutableList<VideoItem> = mutableListOf()
        
        try {
            // Use config.searchResultClass to find video elements
            val container = doc.select(".${config.searchResultClass}")
            
            if (container.isEmpty()) {
                Log.e(TAG, "Container with class ${config.searchResultClass} not found")
                return emptyList()
            }
            
            // Get direct children elements instead of selecting ul
            val videoElements = container.first().children()

            println("Found ${videoElements.size} video elements")
            
            // Skip header if it exists
            for (i in 0 until videoElements.size) {
                val element = videoElements[i]

                println("Processing video element: ${element.html()}")
                
                // Extract video information based on the site structure
                val titleElement = element.select("a").firstOrNull()
                val categoryElement = element.select(".${config.categoryClass}").firstOrNull()
                val updateTimeElement = element.select(".${config.timeClass}").firstOrNull()
                
                if (titleElement != null) {
                    val title = titleElement.text()
                    val url = titleElement.attr("href")
                    val category = categoryElement?.text() ?: ""
                    val updateTime = updateTimeElement?.text() ?: ""
                    
                    videoList.add(
                        VideoItem(
                            title = title,
                            category = category,
                            updateTime = updateTime,
                            url = url
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing search results", e)
        }
        
        return videoList
    }
    
    fun parseDetail(html: String): VideoDetail {
        val doc = Jsoup.parse(html)
        
        try {
            // Get title
            val title = doc.select(config.titleClass!!).text()
            
            // Get cover image URL
            var coverUrl = doc.select(config.vidImgClass!!).attr("src")

            if (coverUrl.startsWith('/')) {
                coverUrl = config.baseUrl + coverUrl
            }
            
            // Get description using config class name
            val description = doc.select(config.descClass!!).firstOrNull()?.text() ?: ""

//            println( doc.select(".playlist.wbox").html())

            // Get episode list
            val episodes = doc.select("${config.className} li").map { element ->
                val number = element.select("span").text().split("$")[0]
                val url = element.select("input[type=checkbox]").attr("value")
                Episode(number, url)
            }.filter {
                it.url.isNotEmpty()
            }
            
            return VideoDetail(title, coverUrl, description, episodes)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing video detail", e)
            return VideoDetail("", "", "", emptyList())
        }
    }
}