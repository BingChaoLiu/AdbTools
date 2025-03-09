package data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.nio.file.Paths

object CommandRepository {
    private val gson = Gson()
    private val appDir = Paths.get(System.getProperty("user.home"), ".adbtool").toString()
    private val commandsFile = File("$appDir/commands.json")

    // 使用 MutableStateFlow 来管理命令列表
    private val _commands = MutableStateFlow<List<AdbCommand>>(emptyList())
    private val commands = _commands.asStateFlow()

    init {
        loadCommands()
    }

    private fun loadCommands() {
        try {
            if (!File(appDir).exists()) {
                File(appDir).mkdirs()
            }

            if (!commandsFile.exists()) {
                // 添加一些默认命令
                val defaultCommands = listOf(
                    AdbCommand(command = "adb shell pm list packages", description = "列出所有安装的包"),
                    AdbCommand(command = "adb shell dumpsys battery", description = "查看电池信息"),
                    AdbCommand(command = "adb shell settings get global airplane_mode_on", description = "获取飞行模式状态"),
                    AdbCommand(command = "adb shell am start -a android.intent.action.MAIN -c android.intent.category.HOME", description = "回到主屏幕"),
                    AdbCommand(command = "adb shell screencap -p /sdcard/screen.png", description = "截图")
                )
                _commands.value = defaultCommands
                saveCommands()
            } else {
                val json = commandsFile.readText()
                val type = object : TypeToken<List<AdbCommand>>() {}.type
                val loadedCommands = gson.fromJson<List<AdbCommand>>(json, type) ?: emptyList()
                _commands.value = loadedCommands
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _commands.value = emptyList()
        }
    }

    private fun saveCommands() {
        try {
            val json = gson.toJson(_commands.value)
            commandsFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 返回 Flow<List<AdbCommand>> 而不是 List<AdbCommand>
    fun getAllCommands(): Flow<List<AdbCommand>> = commands

    fun addCommand(command: AdbCommand) {
        val currentCommands = _commands.value.toMutableList()
        currentCommands.add(command)
        _commands.value = currentCommands
        saveCommands()
    }

    fun removeCommand(id: String) {
        val currentCommands = _commands.value.toMutableList()
        currentCommands.removeIf { it.id == id }
        _commands.value = currentCommands
        saveCommands()
    }
}
