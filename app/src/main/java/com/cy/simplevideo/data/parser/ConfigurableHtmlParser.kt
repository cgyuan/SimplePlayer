package com.cy.simplevideo.data.parser

import android.util.Log
import com.cy.simplevideo.data.config.DataSourceConfig
import com.cy.simplevideo.data.model.Episode
import com.cy.simplevideo.data.model.VideoDetail
import com.cy.simplevideo.data.model.VideoItem
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class ConfigurableHtmlParser(private val config: DataSourceConfig) {

    private val TAG = "ConfigurableHtmlParser"

    fun parseSearchResults(html: String): List<VideoItem> {
        val doc: Document = Jsoup.parse(html)
        val videoList: MutableList<VideoItem> = mutableListOf()

        try {
            // Use config.searchResultClass to find video elements
            val videoElements = doc.select(config.searchResultClass)

            if (videoElements.isEmpty()) {
                Log.e(TAG, "Container with class ${config.searchResultClass} not found")
                return emptyList()
            }

            println("Found ${videoElements.size} video elements")

            // Skip header if it exists
            for (i in 0 until videoElements.size) {
                val element = videoElements[i]

                println("Processing video element: ${element.html()}")

                // Extract video information based on the site structure
                val titleElement = element.select("a").firstOrNull()
                val categoryElement = element.select("${config.categoryClass}").firstOrNull()
                val updateTimeElement = element.select("${config.timeClass}").firstOrNull()

                if (titleElement != null && updateTimeElement != null) {
                    val title = titleElement.text()
                    var url = titleElement.attr("href")

                    if (url.startsWith('/')) {
                        url = config.baseUrl + url
                    }
                    val category = categoryElement?.text() ?: ""
                    val updateTime = updateTimeElement.text() ?: ""

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
            var title = doc.select(config.titleClass!!).first()?.ownText()?.trim() ?: ""

            if (title.startsWith("片名")) {
                title = title.split("：")[1]
            }

            // Get cover image URL
            var coverUrl = doc.select(config.vidImgClass!!).attr("src")

            if (coverUrl.startsWith('/')) {
                coverUrl = config.baseUrl + coverUrl
            }

            // Get description using config class name
            val description = doc.select(config.descClass!!).firstOrNull()?.text() ?: ""

            // Get episode list
            val episodes = doc.select(config.className).map { element ->
                var number = element.select("span").text().split("$")[0]
                var url = element.select("input[type=checkbox]").attr("value")
                if (!url.startsWith("http")) {
                    kotlin.runCatching {
                        val (a, b) = url.split("$")
                        if (number.isEmpty()) {
                            number = a
                        }
                        url = b
                    }
                }

                runCatching {
                    if (url.isEmpty()) {
                        val episodeData =
                            element.select("a").filter { element -> element.text().contains("$") }
                        if (episodeData.isNotEmpty()) {
                            val data = episodeData.first().text()
                            val (a, b) = data.split("$")
                            number = a
                            url = b
                        }
                    }
                }

                println("number: $number, url: $url")

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