package ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import utils.AdbExecutor
import utils.NetworkDeviceScanner

@Composable
fun ConnectDeviceDialog(
    onDismissRequest: () -> Unit,
    onDeviceConnected: () -> Unit
) {
    var ipAddress by remember { mutableStateOf("") }
    var connectionOutput by remember { mutableStateOf("") }
    var isConnecting by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableStateOf(0f) }
    var currentScanIp by remember { mutableStateOf("") }
    var foundDevices by remember { mutableStateOf<List<String>>(emptyList()) }
    
    val scope = rememberCoroutineScope()
    val outputScrollState = rememberScrollState()
    
    // 当输出更新时，自动滚动到底部
    LaunchedEffect(connectionOutput) {
        outputScrollState.animateScrollTo(outputScrollState.maxValue)
    }
    
    // 自动开始扫描网络设备
    LaunchedEffect(Unit) {
        startScan(
            onScanUpdate = { progress ->
                scanProgress = progress.progress
                currentScanIp = progress.currentIp
                foundDevices = progress.foundDevices
                isScanning = !progress.isComplete
            }
        )
    }
    
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp, max = 600.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // 标题和关闭按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "连接设备",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // IP地址输入和连接按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = ipAddress,
                        onValueChange = { ipAddress = it },
                        label = { Text("设备IP地址") },
                        placeholder = { Text("例如: 192.168.1.100") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            scope.launch {
                                isConnecting = true
                                connectionOutput = "正在连接 $ipAddress...\n"
                                
                                val connected = NetworkDeviceScanner.connectToDevice(ipAddress)
                                
                                connectionOutput += if (connected) {
                                    "成功连接到 $ipAddress\n"
                                    onDeviceConnected()
                                    "设备已连接，可以开始使用ADB命令"
                                } else {
                                    "连接失败，请检查设备IP地址和ADB服务是否正常运行"
                                }
                                
                                isConnecting = false
                            }
                        },
                        enabled = ipAddress.isNotBlank() && !isConnecting
                    ) {
                        Text("连接")
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // 网络扫描状态和刷新按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isScanning) {
                        Text(
                            "正在扫描: $currentScanIp",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        
                        CircularProgressIndicator(
                            progress = scanProgress,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            "扫描完成，发现 ${foundDevices.size} 个设备",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        
                        IconButton(
                            onClick = {
                                scope.launch {
                                    startScan(
                                        onScanUpdate = { progress ->
                                            scanProgress = progress.progress
                                            currentScanIp = progress.currentIp
                                            foundDevices = progress.foundDevices
                                            isScanning = !progress.isComplete
                                        }
                                    )
                                }
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "重新扫描")
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                // 设备列表
                Text(
                    "发现的设备:",
                    style = MaterialTheme.typography.titleSmall
                )
                
                if (foundDevices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isScanning) {
                            Text(
                                "正在扫描网络设备...",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        } else {
                            Text(
                                "未发现可连接的设备",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        items(foundDevices) { device ->
                            DeviceItem(
                                ipAddress = device,
                                onClick = {
                                    ipAddress = device
                                }
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // 连接输出
                Text(
                    "连接状态:",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(8.dp)
                ) {
                    if (connectionOutput.isEmpty()) {
                        Text(
                            "等待连接...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(outputScrollState)
                        ) {
                            Text(connectionOutput)
                        }
                    }
                    
                    if (isConnecting) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceItem(
    ipAddress: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(Modifier.width(8.dp))
            
            Text(
                ipAddress,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private suspend fun startScan(
    onScanUpdate: (NetworkDeviceScanner.ScanProgress) -> Unit
) {
    NetworkDeviceScanner.scanNetworkDevices().collect { progress ->
        onScanUpdate(progress)
    }
}