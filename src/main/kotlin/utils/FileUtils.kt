package utils

import java.awt.FileDialog
import java.awt.Frame
import javax.swing.SwingUtilities

object FileUtils {

    fun showFileChooser(onFileSelected: (String) -> Unit) {
        SwingUtilities.invokeLater {
            try {
                // 使用 AWT 的 FileDialog 替代 JFileChooser
                val fileDialog = FileDialog(Frame(), "选择文件", FileDialog.LOAD)
                fileDialog.isVisible = true

                val directory = fileDialog.directory
                val file = fileDialog.file

                if (directory != null && file != null) {
                    val selectedPath = directory + file
                    onFileSelected(selectedPath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}