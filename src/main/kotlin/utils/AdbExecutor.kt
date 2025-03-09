package utils

import data.KeyEvent
import data.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.InputStreamReader

object AdbExecutor {
    fun executeCommand(command: String): Flow<String> = flow {
        val adbPath = SettingsManager.getSettings().adbPath
        // 替换命令中的 "adb" 为配置的路径
        val finalCommand = if (command.startsWith("adb ")) {
            command.replaceFirst("adb", adbPath)
        } else {
            command
        }

        val process = ProcessBuilder("cmd.exe", "/c", finalCommand)
            .redirectErrorStream(true)
            .start()

        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            line?.let { emit(it) }
        }

        process.waitFor()
    }
    .catch { e -> 
            // 使用 catch 操作符处理异常
            emit("错误: ${e.message}")
        }
        .flowOn(Dispatchers.IO) // 使用 flowOn 指定上下文

    suspend fun isDeviceConnected(): Boolean {
        var connected = true
        try {
            val adbPath = SettingsManager.getSettings().adbPath
            executeCommand("$adbPath devices").collect { line ->
                if (line.contains("device") && !line.contains("List of devices attached")) {
                    connected = true
                }
            }
        } catch (e: Exception) {
            // 处理异常但不影响返回结果
            println("检查设备连接时出错: ${e.message}")
        }
        return connected
    }
    
    // 执行按键事件
    fun executeKeyEvent(keyEvent: KeyEvent): Flow<String> {
        val adbPath = SettingsManager.getSettings().adbPath
        val command = "$adbPath shell input keyevent ${keyEvent.keyCode}"
        return executeCommand(command)
}
}
