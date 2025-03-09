import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import data.SettingsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.dialogs.ConnectDeviceDialog
import ui.screens.*
import ui.theme.AdbToolTheme
import utils.AdbExecutor

enum class Screen {
    CUSTOM_COMMANDS,
    KEY_SIMULATION,
    LOG_COLLECTION,
    FILE_OPERATION,
    SETTINGS
}

@Composable
@Preview
fun App(window: ComposeWindow) {
    var currentScreen by remember { mutableStateOf(Screen.CUSTOM_COMMANDS) }
    var isDeviceConnected by remember { mutableStateOf(false) }
    var showConnectDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 确保设置已加载
    LaunchedEffect(Unit) {
        // 设置已在 SettingsManager 的 init 块中加载
        }

    // 定期检查设备连接状态
    LaunchedEffect(Unit) {
        while (true) {
                            isDeviceConnected = AdbExecutor.isDeviceConnected()
            delay(5000) // 每5秒检查一次
                        }
                    }

    AdbToolTheme(darkTheme = true) {
    Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
    ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // 侧边导航栏
                NavigationRail(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface)
                        .width(320.dp)
                        .padding(16.dp),
                    isDeviceConnected = isDeviceConnected,
                    currentScreen = currentScreen,
                    onNavigate = { screen -> currentScreen = screen },
                    onConnectDevice = {
                        showConnectDialog = true
                        }
                )

                // 主内容区域
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    when (currentScreen) {
                        Screen.CUSTOM_COMMANDS -> CustomCommandsScreen(isDeviceConnected)
                        Screen.KEY_SIMULATION -> KeySimulationScreen(isDeviceConnected)
                        Screen.LOG_COLLECTION -> LogCollectionScreen(isDeviceConnected)
                        Screen.FILE_OPERATION -> FileOperationScreen(isDeviceConnected, window)
                        Screen.SETTINGS -> SettingsScreen()
        }
    }
}
            
            // 连接设备对话框
            if (showConnectDialog) {
                ConnectDeviceDialog(
                    onDismissRequest = { showConnectDialog = false },
                    onDeviceConnected = {
                        scope.launch {
                            isDeviceConnected = AdbExecutor.isDeviceConnected()
        }
    }
        )
            }
        }
    }
}

@Composable
fun NavigationRail(
    modifier: Modifier = Modifier,
    isDeviceConnected: Boolean,
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    onConnectDevice: () -> Unit
) {
    Column(modifier = modifier) {
            Text(
            "ADB工具",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
            )

        Spacer(Modifier.height(24.dp))

        // 设备状态指示
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
    ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        if (isDeviceConnected) Color(0xFF4CAF50) else Color(0xFFE53935),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )

            Spacer(Modifier.width(8.dp))

            Text(
                "设备状态: ${if (isDeviceConnected) "已连接" else "未连接"}",
                style = MaterialTheme.typography.bodyMedium
            )
    }

        Spacer(Modifier.height(24.dp))

        // 导航按钮
        NavigationItem(
            icon = Icons.Default.Code,
            label = "自定义命令",
            selected = currentScreen == Screen.CUSTOM_COMMANDS,
            onClick = { onNavigate(Screen.CUSTOM_COMMANDS) }
        )

        NavigationItem(
            icon = Icons.Default.Keyboard,
            label = "按键模拟",
            selected = currentScreen == Screen.KEY_SIMULATION,
            onClick = { onNavigate(Screen.KEY_SIMULATION) }
        )

        NavigationItem(
            icon = Icons.Default.Description,
            label = "日志收集",
            selected = currentScreen == Screen.LOG_COLLECTION,
            onClick = { onNavigate(Screen.LOG_COLLECTION) }
        )

        NavigationItem(
            icon = Icons.Default.Folder,
            label = "文件操作",
            selected = currentScreen == Screen.FILE_OPERATION,
            onClick = { onNavigate(Screen.FILE_OPERATION) }
        )

        NavigationItem(
            icon = Icons.Default.Settings,
            label = "设置",
            selected = currentScreen == Screen.SETTINGS,
            onClick = { onNavigate(Screen.SETTINGS) }
        )

        Spacer(Modifier.weight(1f))

        // 连接设备按钮
        Button(
            onClick = onConnectDevice,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PhoneAndroid, contentDescription = "连接设备")
            Spacer(Modifier.width(8.dp))
            Text("连接设备")
}
    }
}

@Composable
fun NavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = backgroundColor,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = contentColor
            )

            Spacer(Modifier.width(12.dp))

            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ADB工具"
    ) {
        App(window = this.window)
    }
}