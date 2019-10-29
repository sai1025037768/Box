package luyao.box

import luyao.util.ktx.ext.canListFiles
import java.io.*
import java.nio.ByteBuffer
import java.text.DecimalFormat

/**
 * Created by luyao
 * on 2019/7/23 9:29
 */

fun getFolderSize(file: File): Long {
    var total = 0L
    for (subFile in file.listFiles()) {
        total += if (subFile.isFile) subFile.length()
        else getFolderSize(subFile)
    }
    return total
}

fun getFormatFileSize(size: Long, unit: Int = 1000): String {
    val formatter = DecimalFormat("####.00")
    return when {
        size < 0 -> "0 B"
        size < unit -> "$size B"
        size < unit * unit -> "${formatter.format(size / unit)} KB"
        size < unit * unit * unit -> "${formatter.format(size / unit / unit)} MB"
        else -> "${formatter.format(size / unit / unit / unit)} GB"
    }
}


fun getAllSubFile(folder: File): Array<File> {
    var fileList: Array<File> = arrayOf()
    if (!folder.canListFiles) return fileList
    for (subFile in folder.listFiles())
        fileList = if (subFile.isFile) fileList.plus(subFile)
        else fileList.plus(getAllSubFile(subFile))
    return fileList
}

fun copyFile(sourceFile: File, destFile: File, overwrite: Boolean, func: ((file: File, i: Int) -> Unit)? = null) {

    if (!sourceFile.exists()) return

    if (destFile.exists()) {
        val stillExists = if (!overwrite) true else !destFile.delete()

        if (stillExists) {
            return
        }
    }

    if (!destFile.exists()) destFile.createNewFile()

    val inputStream = FileInputStream(sourceFile)
    val outputStream = FileOutputStream(destFile)
    val iChannel = inputStream.channel
    val oChannel = outputStream.channel


    val totalSize = sourceFile.length()
    val buffer = ByteBuffer.allocate(1024)
    var hasRead = 0f
    var progress = -1
    while (true) {
        buffer.clear()
        val read = iChannel.read(buffer)
        if (read == -1)
            break
        buffer.limit(buffer.position())
        buffer.position(0)
        oChannel.write(buffer)
        hasRead += read

        func?.let {
            val newProgress = ((hasRead / totalSize) * 100).toInt()
            if (progress != newProgress) {
                progress = newProgress
                it(sourceFile, progress)
            }
        }
    }

    inputStream.close()
    outputStream.close()
}

fun copyFolder(sourceFolder: File, destFolder: File, overwrite: Boolean, func: ((file: File, i: Int) -> Unit)? = null) {
    if (!sourceFolder.exists()) return

    if (!destFolder.exists()) {
        val result = destFolder.mkdirs()
        if (!result) return
    }

    for (subFile in sourceFolder.listFiles()) {
        if (subFile.isDirectory) {
            copyFolder(subFile, File("${destFolder.path}${File.separator}${subFile.name}"), overwrite, func)
        } else {
            copyFile(subFile, File(destFolder, subFile.name), overwrite, func)
        }
    }
}

fun main() {
    var array: Array<String> = arrayOf()
    array = array.plus("1")
    println(array.size)
}