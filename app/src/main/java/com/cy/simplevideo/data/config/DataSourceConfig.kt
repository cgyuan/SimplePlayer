package com.cy.simplevideo.data.config

data class DataSourceConfig(
    val baseUrl: String,
    val url: String,
    val active: Boolean,
    val isPost: Boolean = false,
    val remark: String,
    val className: String,
    val searchResultClass: String,
    val categoryClass: String? = null,
    val timeClass: String? = null,
    val titleClass: String? = null,
    val vidImgClass: String? = null,
    val descClass: String? = null,
    val searchInputClassName: String,
    val postData: String? = null
)