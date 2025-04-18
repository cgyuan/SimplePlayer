package com.cy.simplevideo.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.cy.simplevideo.data.config.DataSourceConfig
import com.cy.simplevideo.data.model.VideoItem
import com.cy.simplevideo.ui.viewmodel.VideoViewModel
import com.cy.simplevideo.ui.components.DataSourceSelector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: VideoViewModel,
    onVideoClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val dataSources by viewModel.dataSources.collectAsState()
    val activeDataSource by viewModel.activeDataSource.collectAsState()
    
    // 记录上一次的数据源
    var lastDataSource by remember { mutableStateOf<DataSourceConfig?>(null) }

    // 键盘控制器
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // 搜索函数
    val performSearch = {
        if (searchQuery.isNotBlank()) {
            viewModel.searchVideos(searchQuery)
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    // 监听数据源变化，只在数据源真正改变时清空搜索结果
    LaunchedEffect(activeDataSource) {
        if (lastDataSource != null && lastDataSource != activeDataSource) {
//            searchQuery = ""
            viewModel.clearSearchResults()
        }
        lastDataSource = activeDataSource
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("视频搜索") },
                actions = {
                    // 使用新的 DataSourceSelector 组件
                    DataSourceSelector(
                        dataSources = dataSources,
                        activeDataSource = activeDataSource,
                        onDataSourceSelected = { index ->
                            viewModel.setActiveDataSource(index)
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 搜索栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(FocusRequester()),
                    placeholder = { Text("输入关键词搜索") },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { performSearch() }
                    ),
                    singleLine = true,
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    }
                )
                Button(
                    onClick = performSearch
                ) {
                    Text("搜索")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 搜索结果列表
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    error != null -> {
                        Text(
                            text = "搜索失败: $error",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchResults) { video ->
                                VideoItemCard(
                                    video = video,
                                    onClick = { onVideoClick(video.url) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoItemCard(
    video: VideoItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = video.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = video.category,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = video.updateTime,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}