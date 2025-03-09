package utils

import data.SettingsManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import java.io.File

object FileOperationUtils {
    /**
     * 将文本输入到设备
     */
    fun inputText(text: String): Flow<String> {
        // 将空格替换为%s，特殊字符处理
        val escapedText = text.replace(" ", "%s")
                             .replace("\"", "\\\"")
                             .replace("'", "\\'")
                             .replace("(", "\\(")
                             .replace(")", "\\)")
                             .replace("&", "\\&")
                             .replace(";", "\\;")
        
        val command = "adb shell input text \"$escapedText\""
        return AdbExecutor.executeCommand(command)
    }
    
    /**
     * 推送文件到设备
     */
    fun pushFile(sourceFilePath: String, targetPath: String): Flow<String> {
        val command = "adb push \"$sourceFilePath\" \"$targetPath\""
        return AdbExecutor.executeCommand(command)
    }
    
    /**
     * 查看路径信息
     */
    fun listPath(path: String): Flow<String> = flow {
        // 首先检查路径是否存在
        var isDirectory = false
        var exists = false
        
        AdbExecutor.executeCommand("adb shell ls -la \"$path\"").collect { line ->
            if (!line.contains("No such file or directory") && !line.contains("Permission denied")) {
                exists = true
                isDirectory = !line.startsWith("-")
                emit(line)
            }
        }
        
        if (!exists) {
            emit("无效路径: $path")
        } else if (isDirectory) {
            emit("目录: $path")
            // 列出目录内容
            AdbExecutor.executeCommand("adb shell ls -la \"$path\"").collect { line ->
                emit(line)
            }
        } else {
            emit("文件: $path")
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * 从设备拉取文件
     */
    fun pullFile(sourcePath: String, targetPath: String): Flow<String> {
        val command = "adb pull \"$sourcePath\" \"$targetPath\""
        return AdbExecutor.executeCommand(command)
    }
    
    /**
     * 检查路径是否为目录
     */
    suspend fun isDirectory(path: String): Boolean {
        var isDir = false
        AdbExecutor.executeCommand("adb shell [ -d \"$path\" ] && echo \"true\" || echo \"false\"").collect { line ->
            isDir = line.trim() == "true"
        }
        return isDir
    }
    
    /**
     * 检查路径是否为文件
     */
    suspend fun isFile(path: String): Boolean {
        var isFile = false
        AdbExecutor.executeCommand("adb shell [ -f \"$path\" ] && echo \"true\" || echo \"false\"").collect { line ->
            isFile = line.trim() == "true"
        }
        return isFile
    }
}