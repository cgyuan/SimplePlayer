package com.cy.simplevideo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.cy.simplevideo.data.config.DataSourceConfig

@Composable
fun DataSourceSelector(
    dataSources: List<DataSourceConfig>,
    activeDataSource: DataSourceConfig?,
    onDataSourceSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // 当前选中的数据源显示
        Surface(
            onClick = { expanded = true },
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 数据源图标或标识
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = activeDataSource?.remark?.firstOrNull()?.toString() ?: "?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // 数据源名称
                Text(
                    text = activeDataSource?.remark ?: "选择数据源",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 下拉箭头
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "展开数据源列表",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 下拉菜单
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            dataSources.forEachIndexed { index, source ->
                val isSelected = source == activeDataSource
                
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 数据源图标
                                Surface(
                                    modifier = Modifier.size(24.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isSelected) 
                                        MaterialTheme.colorScheme.primaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        Text(
                                            text = source.remark.firstOrNull().toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isSelected)
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // 数据源名称
                                Text(
                                    text = source.remark,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // 选中标记
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "当前选中",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    onClick = {
                        onDataSourceSelected(index)
                        expanded = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
} 