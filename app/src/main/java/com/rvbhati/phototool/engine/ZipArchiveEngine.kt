package com.rvbhati.phototool.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipArchiveEngine {

    suspend fun createZipArchive(
        files: List<File>,
        outputZipFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            outputZipFile.parentFile?.mkdirs()
            ZipOutputStream(FileOutputStream(outputZipFile)).use { zipOut ->
                val buffer = ByteArray(8192)
                for (file in files) {
                    if (file.exists() && file.isFile) {
                        FileInputStream(file).use { fis ->
                            val zipEntry = ZipEntry(file.name)
                            zipOut.putNextEntry(zipEntry)
                            var length: Int
                            while (fis.read(buffer).also { length = it } >= 0) {
                                zipOut.write(buffer, 0, length)
                            }
                            zipOut.closeEntry()
                        }
                    }
                }
            }
            outputZipFile.exists() && outputZipFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
