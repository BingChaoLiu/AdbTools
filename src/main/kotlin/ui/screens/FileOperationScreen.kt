package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import utils.AdbExecutor
import utils.FileOperationUtils
import java.awt.Cursor
import java.awt.FileDialog
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import java.io.File
import javax.swing.SwingUtilities

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FileOperationScreen(isDeviceConnected: Boolean, window: ComposeWindow? = null) {
    var textInput by remember { mutableStateOf("") }
    var pushTargetPath by remember { mutableStateOf("/sdcard/") }
    var pullSourcePath by remember { mutableStateOf("/sdcard/") }
    var operationOutput by remember { mutableStateOf("") }
    var selectedFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    
    val scope = rememberCoroutineScope()
    val outputScrollState = rememberScrollState()
    
    // 当输出更新时，自动滚动到底部
    LaunchedEffect(operationOutput) {
        outputScrollState.animateScrollTo(value = outputScrollState.maxValue)
    }
    
    // 设置文件拖放处理
    LaunchedEffect(window) {
        window?.let { setupFileDragAndDrop(it) { files -> 
            // 添加新文件到现有列表，避免重复
            val existingPaths = selectedFiles.map { it.absolutePath }.toSet()
            val newFiles = files.filter { it.absolutePath !in existingPaths }
            selectedFiles = selectedFiles + newFiles
        }}
    }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            "文件操作",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (!isDeviceConnected) {
            // 未连接设备提示
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "请先连接设备以使用文件操作功能",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // 文本输入区域
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "文本输入",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        "将文本输入到设备中",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            label = { Text("输入文本") },
                            placeholder = { Text("例如: Hello World") },
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    operationOutput = "正在输入文本: $textInput\n"
                                    FileOperationUtils.inputText(textInput).collectLatest { line ->
                                        operationOutput += "$line\n"
                                    }
                                    operationOutput += "文本输入完成\n"
                                }
                            },
                            enabled = isDeviceConnected && textInput.isNotBlank()
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "发送")
                            Spacer(Modifier.width(4.dp))
                            Text("发送")
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // 文件推送区域
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "文件推送",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        "将文件推送到设备中",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // 拖放区域
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(16.dp)
                            .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = "拖放文件",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(32.dp)
                            )
                            
                            Spacer(Modifier.height(8.dp))
                            
                            if (selectedFiles.isEmpty()) {
                                Text(
                                    "拖放文件到此处或点击选择文件",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    "已选择 ${selectedFiles.size} 个文件",
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        // 点击选择文件
                        Button(
                            onClick = {
                                window?.let { win ->
                                    val fileDialog = FileDialog(win, "选择要推送的文件", FileDialog.LOAD).apply {
                                        isMultipleMode = true
                                        isVisible = true
                                    }
                                    
                                    val files = fileDialog.files
                                    if (files.isNotEmpty()) {
                                        // 添加新文件到现有列表，避免重复
                                        val existingPaths = selectedFiles.map { it.absolutePath }.toSet()
                                        val newFiles = files.filter { it.absolutePath !in existingPaths }
                                        selectedFiles = selectedFiles + newFiles
                                    }
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Text("选择文件")
                        }
                    }
                    
                    // 显示已选择的文件列表
                    if (selectedFiles.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "已选择的文件:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            
                            TextButton(
                                onClick = { selectedFiles = emptyList() },
                                enabled = selectedFiles.isNotEmpty()
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "清除所有",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("清除所有")
                            }
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        
                        // 文件列表，固定高度，可滚动
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            LazyColumn(
                                modifier = Modifier.padding(8.dp)
                            ) {
                                items(selectedFiles) { file ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        
                                        Spacer(Modifier.width(8.dp))
                                        
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                file.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            
                                            Text(
                                                file.absolutePath,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        
                                        IconButton(
                                            onClick = {
                                                selectedFiles = selectedFiles.filter { it != file }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "移除",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = pushTargetPath,
                            onValueChange = { pushTargetPath = it },
                            label = { Text("目标路径") },
                            placeholder = { Text("例如: /sdcard/Download/") },
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    operationOutput = "正在推送文件到: $pushTargetPath\n"
                                    
                                    for (file in selectedFiles) {
                                        operationOutput += "推送文件: ${file.name}\n"
                                        FileOperationUtils.pushFile(file.absolutePath, pushTargetPath).collectLatest { line ->
                                            operationOutput += "$line\n"
                                        }
                                    }
                                    
                                    operationOutput += "文件推送完成\n"
                                }
                            },
                            enabled = isDeviceConnected && selectedFiles.isNotEmpty() && pushTargetPath.isNotBlank()
                        ) {
                            Text("推送")
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // 文件拉取区域
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "文件拉取",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        "从设备中拉取文件",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = pullSourcePath,
                            onValueChange = { pullSourcePath = it },
                            label = { Text("源路径") },
                            placeholder = { Text("例如: /sdcard/Download/file.txt") },
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    operationOutput = "正在查看: $pullSourcePath\n"
                                    FileOperationUtils.listPath(pullSourcePath).collectLatest { line ->
                                        operationOutput += "$line\n"
                                    }
                                }
                            },
                            enabled = isDeviceConnected && pullSourcePath.isNotBlank()
                        ) {
                            Text("查看")
                        }
                        
                        Spacer(Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    window?.let { win ->
                                        // 选择保存位置
                                        val fileDialog = FileDialog(win, "选择保存位置", FileDialog.SAVE).apply {
                                            mode = FileDialog.SAVE
                                            directory = System.getProperty("user.home")
                                            file = "选择文件夹" // 这只是一个提示，用户实际上是选择目录
                                            isVisible = true
                                        }
                                        
                                        val directory = fileDialog.directory
                                        if (directory != null) {
                                            val saveDir = File(directory)
                                            
                                            operationOutput = "正在拉取: $pullSourcePath\n到: ${saveDir.absolutePath}\n"
                                            
                                            FileOperationUtils.pullFile(pullSourcePath, saveDir.absolutePath).collectLatest { line ->
                                                operationOutput += "$line\n"
                                            }
                                            
                                            operationOutput += "文件拉取完成\n"
                                        }
                                    }
                                }
                            },
                            enabled = isDeviceConnected && pullSourcePath.isNotBlank()
                        ) {
                            Text("拉取")
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // 操作输出区域
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(4.dp),
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "操作输出:",
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
                    if (operationOutput.isEmpty()) {
                        Text(
                            "等待操作...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(outputScrollState)
                        ) {
                            Text(operationOutput)
                        }
                    }
                }
            }
        }
    }
}

private fun setupFileDragAndDrop(window: ComposeWindow, onFilesDropped: (List<File>) -> Unit) {
    val dropTarget = object : DropTarget() {
        @Synchronized
        override fun drop(event: DropTargetDropEvent) {
            try {
                event.acceptDrop(DnDConstants.ACTION_COPY)
                val transferable = event.transferable
                val data = transferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<*>
                
                val files = data?.filterIsInstance<File>() ?: emptyList()
                SwingUtilities.invokeLater {
                    onFilesDropped(files)
                }
                
                event.dropComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                event.dropComplete(false)
            }
        }
    }
    
    window.contentPane.dropTarget = dropTarget
}