package utils

import data.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit

object NetworkDeviceScanner {
    private const val ADB_PORT = 5555
    private const val CONNECT_TIMEOUT = 1000 // 1秒连接超时

    // 扫描进度和结果的数据类
    data class ScanProgress(
        val currentIp: String = "",
        val progress: Float = 0f,
        val foundDevices: List<String> = emptyList(),
        val isComplete: Boolean = false
    )

    /**
     * 扫描局域网内的 ADB 设备
     * @return Flow<ScanProgress> 返回扫描进度和结果的流
     */
    fun scanNetworkDevices(): Flow<ScanProgress> = flow {
        val foundDevices = mutableListOf<String>()
        val localIps = getLocalIPAddresses()

        for (localIp in localIps) {
            val subnet = localIp.substring(0, localIp.lastIndexOf('.') + 1)
            val total = 255

            for (i in 1..total) {
                val ip = "$subnet$i"
                val progress = i.toFloat() / total

                emit(ScanProgress(ip, progress, foundDevices.toList(), false))

                if (isAdbDeviceAvailable(ip)) {
                    foundDevices.add(ip)
                }
            }
        }

        // 发送完成状态
        emit(ScanProgress(
            currentIp = "",
            progress = 1f,
            foundDevices = foundDevices.toList(),
            isComplete = true
        ))
    }.flowOn(Dispatchers.IO)

    /**
     * 获取本机所有网络接口的 IP 地址
     */
    private fun getLocalIPAddresses(): List<String> {
        val addresses = mutableListOf<String>()
        val interfaces = NetworkInterface.getNetworkInterfaces()

        for (networkInterface in interfaces) {
            if (!networkInterface.isUp || networkInterface.isLoopback) continue

            val interfaceAddresses = networkInterface.interfaceAddresses
            for (interfaceAddress in interfaceAddresses) {
                val address = interfaceAddress.address
                if (address.hostAddress.contains('.')) { // 只处理 IPv4
                    addresses.add(address.hostAddress)
                }
            }
        }
        return addresses
    }

    /**
     * 检查指定 IP 是否有可用的 ADB 设备
     */
    private suspend fun isAdbDeviceAvailable(ip: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // 首先检查端口是否开放
            if (!isPortOpen(ip, ADB_PORT)) {
                return@withContext false
            }

            // 获取配置的 ADB 路径
            val adbPath = SettingsManager.getSettings().adbPath
            // 尝试通过 adb connect 连接设备
            val process = Runtime.getRuntime().exec("$adbPath connect $ip:$ADB_PORT")
            val connected = process.waitFor(2, TimeUnit.SECONDS)

            if (!connected) {
                process.destroy()
                return@withContext false
            }

            // 检查连接结果
            val output = process.inputStream.bufferedReader().readText()
            return@withContext output.contains("connected") || output.contains("already")
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 检查指定 IP 和端口是否开放
     */
    private fun isPortOpen(ip: String, port: Int): Boolean {
        return try {
            val socket = java.net.Socket()
            socket.connect(java.net.InetSocketAddress(ip, port), CONNECT_TIMEOUT)
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 连接到指定的 ADB 设备
     */
    suspend fun connectToDevice(ip: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // 获取配置的 ADB 路径
            val adbPath = SettingsManager.getSettings().adbPath
            val process = Runtime.getRuntime().exec("$adbPath connect $ip:$ADB_PORT")
            val connected = process.waitFor(2, TimeUnit.SECONDS)

            if (!connected) {
                process.destroy()
                return@withContext false
            }

            val output = process.inputStream.bufferedReader().readText()
            return@withContext output.contains("connected") || output.contains("already")
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
