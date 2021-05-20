package glitterFrameWork.util

import com.glitter.server.PublicBeans
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.request.*
import io.ktor.response.respondFile
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder

object File_Refresh {
    var allpath = ArrayList<String>()
    fun fileRefresh(rout: Routing, root: Array<String>, defineAppRout: MutableMap<String, String>) {
        Thread {
            for (a in defineAppRout) {
                if (!File(a.value).exists()) {
                    File(a.value).mkdirs()
                    ZipUtil.unzip(PublicBeans.glitterInputStream!!.inputStream(), a.value)
                }
            }
            try {
                rout {
                    for (a in defineAppRout) {
                        get("/${a.key}/{...}") {
                            var relativePath =
                                a.value + call.request.path().replace("/${a.key}", "").replace("%20", " ")
                            relativePath = URLDecoder.decode(relativePath, "UTF-8")
                            println("relativePath:${relativePath}")
                            var i = File(relativePath)
                            if (!i.exists()) {
                                i = File("$relativePath.html")
                            }
                            if (i.isFile && i.exists()) {
                                when (i.extension) {
                                    "html" -> {
                                        call.respondText(i.readText(), ContentType.Text.Html)
                                    }
                                    else -> {
                                        call.respondFile(i)
                                    }
                                }
                            } else {
                                call.respondText("File is not exists")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Thread.sleep(1000 * 30)
        }.start()
    }
}

object File_util {
    fun getAllFiles(file: File): ArrayList<File> {
        val fileList = ArrayList<File>()
        for (i in file.listFiles()) {
            fileList.add(i)
            if (i.isDirectory) {
                fileList.addAll(getAllFiles(i))
            }
        }
        return fileList
    }
}