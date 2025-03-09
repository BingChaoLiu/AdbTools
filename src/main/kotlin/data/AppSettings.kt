package data

import com.google.gson.Gson
import java.io.File
import java.nio.file.Paths

data class AppSettings(
    var adbPath: String = "adb" // 默认使用系统 PATH 中的 adb
)

object SettingsManager {
    private val gson = Gson()
    private val appDir = Paths.get(System.getProperty("user.home"), ".adbtool").toString()
    private val settingsFile = File("$appDir/settings.json")
    
    private var settings = AppSettings()
    
    init {
        loadSettings()
    }
    
    fun getSettings(): AppSettings = settings
    
    fun saveSettings(newSettings: AppSettings) {
        settings = newSettings
        try {
            if (!File(appDir).exists()) {
                File(appDir).mkdirs()
            }
            
            val json = gson.toJson(settings)
            settingsFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun loadSettings() {
        try {
            if (settingsFile.exists()) {
                val json = settingsFile.readText()
                settings = gson.fromJson(json, AppSettings::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}