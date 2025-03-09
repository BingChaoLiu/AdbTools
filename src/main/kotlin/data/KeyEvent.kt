package data

import java.util.UUID

data class KeyEvent(
    val id: String = UUID.randomUUID().toString(), // 使用 UUID 生成唯一 ID
    val name: String,         // 按键名称
    val keyCode: Int,         // Android KeyEvent 值
    val description: String = "", // 按键描述（可选）
    val isCustom: Boolean = false // 是否为自定义按键
)

// 预定义的常用按键
object PredefinedKeys {
    val keys = listOf(
        KeyEvent(name = "HOME", keyCode = 3, description = "主屏幕键"),
        KeyEvent(name = "BACK", keyCode = 4, description = "返回键"),
        KeyEvent(name = "MENU", keyCode = 82, description = "菜单键"),
        KeyEvent(name = "POWER", keyCode = 26, description = "电源键"),
        KeyEvent(name = "VOLUME UP", keyCode = 24, description = "音量+"),
        KeyEvent(name = "VOLUME DOWN", keyCode = 25, description = "音量-"),
        KeyEvent(name = "MUTE", keyCode = 164, description = "静音"),
        KeyEvent(name = "ENTER", keyCode = 66, description = "确认/回车"),
        KeyEvent(name = "DPAD UP", keyCode = 19, description = "方向键上"),
        KeyEvent(name = "DPAD DOWN", keyCode = 20, description = "方向键下"),
        KeyEvent(name = "DPAD LEFT", keyCode = 21, description = "方向键左"),
        KeyEvent(name = "DPAD RIGHT", keyCode = 22, description = "方向键右"),
        KeyEvent(name = "DPAD CENTER", keyCode = 23, description = "方向键中心"),
        KeyEvent(name = "CAMERA", keyCode = 27, description = "相机键"),
        KeyEvent(name = "SEARCH", keyCode = 84, description = "搜索键"),
        KeyEvent(name = "APP SWITCH", keyCode = 187, description = "应用切换键"),
        KeyEvent(name = "BRIGHTNESS UP", keyCode = 221, description = "亮度+"),
        KeyEvent(name = "BRIGHTNESS DOWN", keyCode = 220, description = "亮度-"),
        KeyEvent(name = "PLAY", keyCode = 126, description = "播放"),
        KeyEvent(name = "PAUSE", keyCode = 127, description = "暂停"),
        KeyEvent(name = "MEDIA NEXT", keyCode = 87, description = "下一曲"),
        KeyEvent(name = "MEDIA PREVIOUS", keyCode = 88, description = "上一曲"),
        KeyEvent(name = "MEDIA STOP", keyCode = 86, description = "停止播放")
    )
}