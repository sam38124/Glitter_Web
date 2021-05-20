package glitterFrameWork.util


import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipUtil{
     fun zipFile(filePath: String) {
        try {
            val file = File(filePath)
            val zipFileName: String = file.name +(".zip")
            val fos = FileOutputStream(zipFileName)
            val zos = ZipOutputStream(fos)
            zos.putNextEntry(ZipEntry(file.name))
            val bytes: ByteArray = Files.readAllBytes(Paths.get(filePath))
            zos.write(bytes, 0, bytes.size)
            zos.closeEntry()
            zos.close()
        } catch (ex: FileNotFoundException) {
            System.err.format("The file %s does not exist", filePath)
        } catch (ex: IOException) {
            System.err.println("I/O error: $ex")
        }
    }
    //解壓縮檔案
    fun unzip(zipFilePath: ByteArrayInputStream, destDirectory: String) {
        val destDir = File(destDirectory)
        if (!destDir.exists()) {
            destDir.mkdir()
        }
        val zipIn = ZipInputStream((zipFilePath))
        var entry = zipIn.nextEntry
        while (entry != null) {
            val filePath = destDirectory + File.separator.toString() + entry.name.replace("HellowWorld/","").replace("HellowWorld\\","")
            if(!filePath.contains("__MACOSX")){
                if (!entry.isDirectory) {
                    // if the entry is a file, extracts it
                    extractFile(zipIn, filePath)
                } else {
                    // if the entry is a directory, make the directory
                    val dir = File(filePath)
                    dir.mkdirs()
                }
            }
            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }
        zipIn.close()
    }
    @Throws(IOException::class)
    private fun extractFile(zipIn: ZipInputStream, filePath: String) {
        File(filePath).parentFile.mkdirs()
        val bos = BufferedOutputStream(FileOutputStream(filePath))
        val bytesIn = ByteArray(4096)
        var read = 0
        while (zipIn.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)
        }
        bos.close()
    }
}

