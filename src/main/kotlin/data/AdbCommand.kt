package data

data class AdbCommand(
    val id: String = System.currentTimeMillis().toString(),
    val command: String,
    val description: String = ""
)