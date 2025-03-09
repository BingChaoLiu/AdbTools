package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import data.AdbCommand
import data.CommandRepository
import kotlinx.coroutines.launch
import utils.AdbExecutor

@Composable
fun CustomCommandsScreen(isDeviceConnected: Boolean) {
    // 修改为 collectAsState() 以实时更新命令列表
    val commands by CommandRepository.getAllCommands().collectAsState(initial = emptyList())
    var commandInput by remember { mutableStateOf("") }
    var commandOutput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // 创建一个滚动状态，用于命令输出区域
    val scrollState = rememberScrollState()
    
    // 当输出更新时，自动滚动到底部
    LaunchedEffect(commandOutput) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            "自定义命令",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 命令输入区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                placeholder = { Text("输入ADB命令，例如: adb shell pm list packages") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = {
                    if (commandInput.isNotBlank()) {
                        scope.launch {
                            CommandRepository.addCommand(AdbCommand(command = commandInput))
                            commandInput = ""
                        }
                    }
                },
                enabled = commandInput.isNotBlank()
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加")
                Spacer(Modifier.width(4.dp))
                Text("添加命令")
            }
        }

        Spacer(Modifier.height(16.dp))

        // 命令列表
        if (commands.isEmpty()) {
            // 添加空状态显示
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "暂无自定义命令",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(
                    items = commands,
                    key = { it.id } // 添加 key 以优化性能
                ) { command ->
                    CommandItem(
                        command = command,
                        isDeviceConnected = isDeviceConnected,
                        onExecute = { cmd ->
                            scope.launch {
                                commandOutput = ""
                                AdbExecutor.executeCommand(cmd).collect { line ->
                                    commandOutput += "$line\n"
                                }
                            }
                        },
                        onDelete = { id ->
                            CommandRepository.removeCommand(id)
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 命令输出区域 - 修改为可滚动
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(4.dp),
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "命令输出:",
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
                    if (commandOutput.isEmpty() && !isDeviceConnected) {
                        Text(
                            "$ 请先连接设备...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    } else if (commandOutput.isEmpty()) {
                        Text(
                            "$ 等待命令执行...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    } else {
                        // 使用 verticalScroll 修饰符使内容可滚动
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        ) {
                            Text(
                                "$ $commandOutput",
                                color = MaterialTheme.colorScheme.onSurface
                            )
            }
        }
    }
}
        }
    }
}

// CommandItem 保持不变
@Composable
fun CommandItem(
    command: AdbCommand,
    isDeviceConnected: Boolean,
    onExecute: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
            )

            Spacer(Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    command.command,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (command.description.isNotBlank()) {
                    Text(
                        command.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            IconButton(
                onClick = { onDelete(command.id) }
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.width(4.dp))

            Button(
                onClick = { onExecute(command.command) },
                enabled = isDeviceConnected
            ) {
                Text("执行")
            }
        }
    }
}
