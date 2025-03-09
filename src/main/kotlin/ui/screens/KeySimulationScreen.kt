package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import data.KeyEvent
import data.KeyEventRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import utils.AdbExecutor

@Composable
fun KeySimulationScreen(isDeviceConnected: Boolean) {
    val keys by KeyEventRepository.getAllKeys().collectAsState(initial = emptyList())
    var showAddKeyDialog by remember { mutableStateOf(false) }
    var keyOutput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val outputScrollState = rememberScrollState()
    
    // 当输出更新时，自动滚动到底部
    LaunchedEffect(keyOutput) {
        outputScrollState.animateScrollTo(value = outputScrollState.maxValue)
    }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // 标题和添加按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "按键模拟",
                style = MaterialTheme.typography.titleLarge
            )
            
            Button(
                onClick = { showAddKeyDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加按键")
                Spacer(Modifier.width(4.dp))
                Text("添加自定义按键")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        if (!isDeviceConnected) {
            // 未连接设备提示
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "请先连接设备以使用按键模拟功能",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
        }
        } else if (keys.isEmpty()) {
            // 无按键提示
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "暂无可用按键",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
    }
        } else {
            // 按键网格
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = keys,
                    // 使用组合键作为唯一标识符，确保不会重复
                    key = { "${it.id}_${it.name}_${it.keyCode}" }
                ) { keyEvent ->
                    KeyEventItem(
                        keyEvent = keyEvent,
                        isDeviceConnected = isDeviceConnected,
                        onClick = {
                            scope.launch {
                                keyOutput += "执行按键: ${keyEvent.name} (keycode: ${keyEvent.keyCode})\n"
                                AdbExecutor.executeKeyEvent(keyEvent).collectLatest { line ->
                                    if (line.isNotBlank()) {
                                        keyOutput += "$line\n"
}
                                }
                                keyOutput += "按键执行完成\n"
                            }
                        },
                        onDelete = { id ->
                            if (keyEvent.isCustom) {
                                KeyEventRepository.removeKey(id)
                            }
                        }
                    )
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // 按键执行输出区域
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(4.dp),
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "按键执行输出:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    if (keyOutput.isEmpty()) {
                        Text(
                            "点击按键开始执行...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(outputScrollState)
                        ) {
                            Text(keyOutput)
                        }
                    }
                }
            }
        }
    }
    
    // 添加自定义按键对话框
    if (showAddKeyDialog) {
        AddKeyEventDialog(
            onDismiss = { showAddKeyDialog = false },
            onAddKey = { name, keyCode, description ->
                KeyEventRepository.addCustomKey(
                    KeyEvent(
                        name = name,
                        keyCode = keyCode,
                        description = description,
                        isCustom = true
                    )
                )
                showAddKeyDialog = false
            }
        )
    }
}

@Composable
fun KeyEventItem(
    keyEvent: KeyEvent,
    isDeviceConnected: Boolean,
    onClick: () -> Unit,
    onDelete: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .clickable(enabled = isDeviceConnected, onClick = onClick)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    keyEvent.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                if (keyEvent.isCustom) {
                    IconButton(
                        onClick = { onDelete(keyEvent.id) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                "KeyCode: ${keyEvent.keyCode}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            if (keyEvent.description.isNotBlank()) {
                Text(
                    keyEvent.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AddKeyEventDialog(
    onDismiss: () -> Unit,
    onAddKey: (name: String, keyCode: Int, description: String) -> Unit
) {
    var keyName by remember { mutableStateOf("") }
    var keyCodeText by remember { mutableStateOf("") }
    var keyDescription by remember { mutableStateOf("") }
    var keyCodeError by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    "添加自定义按键",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = keyName,
                    onValueChange = { keyName = it },
                    label = { Text("按键名称") },
                    placeholder = { Text("例如: 返回键") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = keyCodeText,
                    onValueChange = { 
                        keyCodeText = it
                        keyCodeError = it.toIntOrNull() == null && it.isNotEmpty()
                    },
                    label = { Text("按键代码 (KeyCode)") },
                    placeholder = { Text("例如: 4") },
                    singleLine = true,
                    isError = keyCodeError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        if (keyCodeError) {
                            Text("请输入有效的数字")
                        }
                    }
                )
                
                Spacer(Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = keyDescription,
                    onValueChange = { keyDescription = it },
                    label = { Text("按键描述 (可选)") },
                    placeholder = { Text("例如: 返回上一页") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("取消")
                    }
                    
                    Spacer(Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            val keyCode = keyCodeText.toIntOrNull() ?: 0
                            onAddKey(keyName, keyCode, keyDescription)
                        },
                        enabled = keyName.isNotBlank() && !keyCodeError && keyCodeText.isNotBlank()
                    ) {
                        Text("添加")
                    }
                }
            }
        }
    }
}