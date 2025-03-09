package data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.nio.file.Paths

object KeyEventRepository {
    private val gson = Gson()
    private val appDir = Paths.get(System.getProperty("user.home"), ".adbtool").toString()
    private val keysFile = File("$appDir/custom_keys.json")
    
    // 使用 StateFlow 存储所有按键（预定义 + 自定义）
    private val _allKeys = MutableStateFlow<List<KeyEvent>>(emptyList())
    private val allKeysFlow = _allKeys.asStateFlow()
    
    init {
        loadKeys()
    }
    
    fun getAllKeys(): Flow<List<KeyEvent>> = allKeysFlow
    
    private fun loadKeys() {
        try {
            if (!File(appDir).exists()) {
                File(appDir).mkdirs()
            }
            
            val customKeys = if (keysFile.exists()) {
                val json = keysFile.readText()
                val type = object : TypeToken<List<KeyEvent>>() {}.type
                gson.fromJson<List<KeyEvent>>(json, type) ?: emptyList()
            } else {
                emptyList()
            }
            
            // 合并预定义按键和自定义按键
            _allKeys.value = PredefinedKeys.keys + customKeys
        } catch (e: Exception) {
            e.printStackTrace()
            _allKeys.value = PredefinedKeys.keys
        }
    }
    
    private fun saveCustomKeys() {
        try {
            val customKeys = _allKeys.value.filter { it.isCustom }
            val json = gson.toJson(customKeys)
            keysFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun addCustomKey(keyEvent: KeyEvent) {
        _allKeys.update { currentKeys ->
            val newKey = keyEvent.copy(isCustom = true)
            val updatedKeys = currentKeys + newKey
            saveCustomKeys()
            updatedKeys
        }
    }
    
    fun removeKey(id: String) {
        _allKeys.update { currentKeys ->
            val updatedKeys = currentKeys.filter { it.id != id || !it.isCustom }
            saveCustomKeys()
            updatedKeys
        }
    }
}