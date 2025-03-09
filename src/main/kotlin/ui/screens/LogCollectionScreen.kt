package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LogCollectionScreen(isDeviceConnected: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "日志收集",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(Modifier.height(16.dp))
        
        if (!isDeviceConnected) {
            Text("请先连接设备", color = MaterialTheme.colorScheme.error)
        } else {
            Text("日志收集功能开发中...")
        }
    }
}