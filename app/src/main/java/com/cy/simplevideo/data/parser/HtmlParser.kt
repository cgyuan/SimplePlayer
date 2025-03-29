package com.cy.simplevideo.data.parser

import android.util.Log
import com.cy.simplevideo.data.model.VideoItem
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

object HtmlParser {
    fun parseSearchResults(html: String): List<VideoItem> {
        val doc: Document = Jsoup.parse(html)
        val videoList: MutableList<VideoItem> = mutableListOf()
        
        // 查找所有视频项
        val videoElements: Elements = doc.select("div.xing_vb ul")
        
        // 跳过表头
        for (i in 1 until videoElements.size) {
            val element: Element = videoElements[i]
            
            // 提取视频信息
            val titleElement = element.select("span.xing_vb4 a").first()
            val categoryElement = element.select("span.xing_vb5").first()
            val updateTimeElement = element.select("span.xing_vb7").first()

            Log.d("HtmlParser", "title: ${titleElement?.text()}, category: ${categoryElement?.text()}, updateTime: ${updateTimeElement?.text()}")
            
            if (titleElement != null && categoryElement != null && updateTimeElement != null) {
                val title = titleElement.text()
                val category = categoryElement.text()
                val updateTime = updateTimeElement.text()
                val url = titleElement.attr("href")
                
                videoList.add(VideoItem(
                    title = title,
                    category = category,
                    updateTime = updateTime,
                    url = url
                ))
            }
        }
        
        return videoList
    }
} 