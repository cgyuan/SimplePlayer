package com.cy.simplevideo.data.model

data class VideoDetail(
    val title: String,
    val coverUrl: String,
    val description: String,
    val episodes: List<Episode>
)

data class Episode(
    val number: String,
    val url: String
) 