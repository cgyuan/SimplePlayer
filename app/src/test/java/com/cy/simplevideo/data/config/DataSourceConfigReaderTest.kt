package com.cy.simplevideo.data.config

import android.content.Context
import android.content.res.AssetManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.io.ByteArrayInputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class DataSourceConfigReaderTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var assetManager: AssetManager

    private val testJson = """
        [
            {
                "url": "https://test1.com/search?wd=",
                "active": true,
                "isPost": false,
                "remark": "Test1",
                "className": "test-class1",
                "searchResultClass": "search-result1",
                "searchInputClassName": "search-input1"
            },
            {
                "url": "https://test2.com/search?wd=",
                "active": false,
                "remark": "Test2",
                "className": "test-class2",
                "searchResultClass": "search-result2",
                "searchInputClassName": "search-input2"
            }
        ]
    """.trimIndent()

    @Before
    fun setup() {
        `when`(context.assets).thenReturn(assetManager)
        `when`(assetManager.open("data_sources.json")).thenReturn(
            ByteArrayInputStream(testJson.toByteArray())
        )
    }

    @Test
    fun `test getDataSources returns correct list of sources`() {
        val sources = DataSourceConfigReader.getDataSources(context)
        
        assertEquals(2, sources.size)
        
        val firstSource = sources[0]
        assertEquals("https://test1.com/search?wd=", firstSource.url)
        assertTrue(firstSource.active)
        assertEquals("Test1", firstSource.remark)
        assertEquals("test-class1", firstSource.className)
        
        val secondSource = sources[1]
        assertEquals("https://test2.com/search?wd=", secondSource.url)
        assertTrue(!secondSource.active)
        assertEquals("Test2", secondSource.remark)
        assertEquals("test-class2", secondSource.className)
    }

    @Test
    fun `test getActiveDataSource returns only active source`() {
        val activeSource = DataSourceConfigReader.getActiveDataSource(context)
        
        assertNotNull(activeSource)
        assertEquals("https://test1.com/search?wd=", activeSource.url)
        assertTrue(activeSource.active)
        assertEquals("Test1", activeSource.remark)
    }

} 