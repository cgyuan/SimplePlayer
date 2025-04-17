package com.cy.simplevideo.widgets

import android.content.Context
import android.util.Log
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class AdFilteringVideoPlayer(context: Context) : FullScreenVideoPlayer(context) {
    companion object {
        private const val TAG = "AdFilteringVideoPlayer"
    }

    private var tempFile: File? = null
    private var isProcessing = false

    override fun startPlayLogic() {
        if (mUrl != null && mUrl.endsWith(".m3u8") && !isProcessing) {
            isProcessing = true
            Thread {
                try {
                    val processedContent = processM3U8Recursive(mUrl)
                    Log.d(TAG, "Processed content: $processedContent")

                    // 保存处理后的内容到临时文件
                    val cacheDir = context.cacheDir
                    tempFile = File(cacheDir, "filtered_${System.currentTimeMillis()}.m3u8")
                    tempFile?.writeText(processedContent)
                    mUrl = tempFile?.toURI()?.toString() ?: mUrl

                    post {
                        super.startPlayLogic()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing M3U8", e)
                    post {
                        super.startPlayLogic()
                    }
                } finally {
                    isProcessing = false
                }
            }.start()
        } else {
            super.startPlayLogic()
        }
    }

    private fun processM3U8Recursive(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        
        val content = connection.inputStream.bufferedReader().use { it.readText() }
        val lines = content.lines()
        val baseUrl = url.substring(0, url.lastIndexOf("/") + 1)

        // 检查是否是主播放列表
        if (lines.any { it.startsWith("#EXT-X-STREAM-INF:") }) {
            Log.d(TAG, "Found master playlist")
            // 找到最高质量的流
            var maxBandwidth = 0
            var selectedStream: String? = null
            
            lines.forEachIndexed { index, line ->
                if (line.startsWith("#EXT-X-STREAM-INF:")) {
                    val bandwidthMatch = "BANDWIDTH=(\\d+)".toRegex().find(line)
                    val bandwidth = bandwidthMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
                    
                    if (bandwidth > maxBandwidth && index + 1 < lines.size) {
                        maxBandwidth = bandwidth
                        val streamUrl = lines[index + 1]
                        selectedStream = if (streamUrl.startsWith("http")) {
                            streamUrl
                        } else {
                            baseUrl + streamUrl
                        }
                    }
                }
            }
            
            // 递归处理选中的子播放列表
            return if (selectedStream != null) {
                processM3U8Recursive(selectedStream)
            } else {
                content
            }
        } else {
            Log.d(TAG, "Found media playlist")
            // 处理媒体播放列表
            val processedLines = mutableListOf<String>()
            var lastExtInf: String? = null
            var segmentCount = 0
            var totalDuration = 0.0
            var currentSegmentDuration = 0.0
            
            lines.forEach { line ->
                when {
                    line.startsWith("#EXTM3U") ||
                    line.startsWith("#EXT-X-VERSION:") ||
                    line.startsWith("#EXT-X-TARGETDURATION:") ||
                    line.startsWith("#EXT-X-MEDIA-SEQUENCE:") ||
                    line.startsWith("#EXT-X-PLAYLIST-TYPE:") -> {
                        processedLines.add(line)
                    }
                    line.startsWith("#EXT-X-DISCONTINUITY") -> {
                        // 忽略这个标签以过滤广告
                    }
                    line.startsWith("#EXTINF:") -> {
                        val durationStr = line.substring(8, line.indexOf(','))
                        currentSegmentDuration = durationStr.toDoubleOrNull() ?: 0.0
                        lastExtInf = line
                        segmentCount++
                        totalDuration += currentSegmentDuration
                    }
                    line.isNotBlank() && !line.startsWith("#") -> {
                        if (lastExtInf != null) {
                            processedLines.add(lastExtInf)
                            val mediaUrl = if (line.startsWith("http")) line else baseUrl + line
                            processedLines.add(mediaUrl)
                        }
                        lastExtInf = null
                    }
                    line.startsWith("#EXT-X-ENDLIST") -> {
                        processedLines.add(line)
                    }
                }
            }
            
            return processedLines.joinToString("\n")
        }
    }

    override fun release() {
        super.release()
        tempFile?.delete()
        tempFile = null
        isProcessing = false
    }
} 