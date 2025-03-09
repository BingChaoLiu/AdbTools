package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.AppSettings
import data.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import utils.FileUtils.showFileChooser
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun SettingsScreen() {
    val settings = SettingsManager.getSettings()
    var adbPath by remember { mutableStateOf(settings.adbPath) }
    var showSaveSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            "设置",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // ADB 路径设置
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "ADB 工具设置",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "请配置 ADB 可执行文件的路径。如果 ADB 已添加到系统环境变量，可以直接使用 'adb'。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = adbPath,
                        onValueChange = { adbPath = it },
                        label = { Text("ADB 路径") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    Spacer(Modifier.width(8.dp))

                    // 文件选择按钮
                    Button(
                        onClick = {
                            showFileChooser { selectedPath ->
                                adbPath = selectedPath
                            }
                        }
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "选择文件")
                    }
                }

                Spacer(Modifier.height(24.dp))

                // 验证 ADB 路径
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                val testCommand = if (adbPath == "adb") "adb version" else "$adbPath version"
                                var isValid = false
                                var errorMessage = ""

                                try {
                                    withContext(Dispatchers.IO) {  // 使用 withContext 切换到 IO 线程
                                        val process = ProcessBuilder("cmd.exe", "/c", testCommand)
                                            .redirectErrorStream(true)
                                            .start()

                                        val reader = BufferedReader(InputStreamReader(process.inputStream))
                                        val output = reader.readText()

                                        isValid = output.contains("Android Debug Bridge")
                                        if (!isValid) {
                                            errorMessage = "无法验证 ADB 路径: $output"
                                        }

                                        process.waitFor()
                                    }

                                    // 回到主线程处理结果
                                    if (isValid) {
                                        // 保存设置
                                        SettingsManager.saveSettings(AppSettings(adbPath = adbPath))
                                        showSaveSuccess = true
                                    } else {
                                        // 显示错误
                                        // 这里可以添加一个错误提示对话框
                                    }
                                } catch (e: Exception) {
                                    // 回到主线程处理异常
                                    isValid = false
                                    errorMessage = "错误: ${e.message}"
                                    // 显示错误提示
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "保存")
                        Spacer(Modifier.width(4.dp))
                        Text("保存设置")
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // 其他设置项可以在这里添加
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "关于",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(16.dp))
                Text(
                    "ADB 工具是一个用于与 Android 设备进行交互的桌面应用程序。\n" +
                            "版本: 1.0.0\n" +
                            "© 2025 ADB 工具团队",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    // 保存成功提示
    if (showSaveSuccess) {
        AlertDialog(
            onDismissRequest = { showSaveSuccess = false },
            title = { Text("保存成功") },
            text = { Text("ADB 路径设置已保存。") },
            confirmButton = {
                Button(
                    onClick = { showSaveSuccess = false }
                ) {
                    Text("确定")
                }
            }
        )
    }
}