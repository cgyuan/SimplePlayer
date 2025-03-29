package com.cy.simplevideo.data.parser

import com.cy.simplevideo.data.model.Episode
import com.cy.simplevideo.data.model.VideoDetail
import org.jsoup.Jsoup

object DetailParser {
    fun parseDetail(html: String): VideoDetail {
        val doc = Jsoup.parse(html)

        // 获取标题
        val title = doc.select("div.vodh h2").text()

        // 获取封面图片URL
        val coverUrl = doc.select("div.vodImg img").attr("src")

        // 获取描述
        val description = doc.select("div.vodplayinfo").first()?.text() ?: ""

        // 获取剧集列表
        val episodes = doc.select("div.vodplayinfo ul li").map { element ->
            val number = element.select("span").text().split("$")[0]
            val url = element.select("input[type=checkbox]").attr("value")
            Episode(number, url)
        }

        return VideoDetail(title, coverUrl, description, episodes)
    }
}