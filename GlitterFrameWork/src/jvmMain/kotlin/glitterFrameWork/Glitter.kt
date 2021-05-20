package glitterFrameWork

import com.glitter.server.PublicBeans
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import glitterFrameWork.JavaScriptInterFace.Companion.javaScriptInterFace
import glitterFrameWork.util.File_Refresh
import glitterFrameWork.util.SqLiteConnector
import glitterFrameWork.util.ZipUtil
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.respondFile
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.sessions.sessions
import java.io.File
import java.io.FileInputStream
import java.sql.ResultSetMetaData
import java.util.*
import kotlin.collections.ArrayList

object Glitter {

    data class ProData(var data: String)
    data class DataBase(var data: String)

    fun setUp(
        rout: Routing,
        session: (Sessions.Configuration) -> Unit,
        defineAppRout: MutableMap<String, String> = mutableMapOf()
    ) {
        //創建Glitter資料夾路徑
        val glitterRout = File("Glitter")
        if (!glitterRout.exists()) {
            glitterRout.mkdir()
        }
        while (PublicBeans.glitterInputStream == null) {
            PublicBeans.glitterInputStream =
                JzUtil.getRequest("https://storage.googleapis.com/squarestudio/Glitter_1.0.zip", 30 * 1000)!!
                    .toByteArray()
        }
        val dataBaseRout = File("dataBase")
        if (!dataBaseRout.exists()) {
            dataBaseRout.mkdir()
        }
        rout {
            install(Sessions) {
                cookie<ProData>("Pro")
                cookie<DataBase>("DataBase")
                session(this)
            }
            //文件更新
            File_Refresh.fileRefresh(rout, arrayOf("Glitter"), defineAppRout)
            //取得DataBase
            post("/DataBase/initByFile") {
                val jsonMap: MutableMap<String, Any> = Gson().fromJson<MutableMap<String, Any>>(call.receiveText(),
                    object : TypeToken<MutableMap<String, Any>>() {}.type)
                val pro = call.sessions.get("DataBase")
                var data: MutableMap<String, String> = mutableMapOf()
                if (pro != null) {
                    data = Gson().fromJson<MutableMap<String, String>>(
                        if (pro.toString().isEmpty()) "{}" else (pro as Glitter.DataBase).data, object :
                            TypeToken<MutableMap<String, String>>() {}.type
                    )
                }
                data[jsonMap["dataBase"].toString()] = jsonMap["rout"].toString()
                call.sessions.set("DataBase", Glitter.DataBase(Gson().toJson(data)))
                println("setDataBase--${call.sessions.get("DataBase")}")
                call.respondText("true")
            }
            //定義你的JavaScript函式
            post("/RunJsInterFace") {
                val mapData = Gson().fromJson<MutableMap<String, Any>>(
                    call.receiveText(),
                    object : TypeToken<MutableMap<String, Any>>() {}.type
                )

                val functionName = mapData["functionName"] as String
                val receiveValue: MutableMap<String, Any> =
                    if (mapData["data"] == null) mutableMapOf() else (mapData["data"] as MutableMap<String, Any>)

                val cFunction = javaScriptInterFace.filter { it.functionName == functionName }
                val requestFunction = RequestFunction(receiveValue)
                if (cFunction.isNotEmpty()) {
                    cFunction[0].function(requestFunction)
                }
                call.respondText(Gson().toJson(requestFunction.responseValue), ContentType.Text.Plain)
            }
            //定義你的API函式
            post("/PostApi") {
                val mapData = Gson().fromJson<MutableMap<String, Any>>(
                    call.receiveText(),
                    object : TypeToken<MutableMap<String, Any>>() {}.type
                )
                val functionName = mapData["functionName"] as String
                val routName = mapData["routName"] as String
                val receiveValue: MutableMap<String, Any> =
                    if (mapData["data"] == null) mutableMapOf() else (mapData["data"] as MutableMap<String, Any>)
                val cFunction = ApiInterFace.apiInterFace.filter {
                    (it.functionName == functionName) && (it.rout == routName)
                }
                val requestFunction = RequestFunction(receiveValue)
                if (cFunction.isNotEmpty()) {
                    cFunction[0].function(requestFunction)
                }else{
                    requestFunction.responseValue["result"]="Function is not define"
                }
                if(ApiInterFace.deBugMode){
                    println("functionName->${functionName}")
                    println("requestValue->${requestFunction.receiveValue}")
                    println("functionName->${requestFunction.responseValue}")
                }
                call.respondText(Gson().toJson(requestFunction.responseValue), ContentType.Text.Plain)
            }
            //查詢DataBase
            post("/DataBase/Query") {
                val returnMap: ArrayList<MutableMap<String, Any>> = ArrayList<MutableMap<String, Any>>()
                val jsonMap: MutableMap<String, Any> = Gson().fromJson<MutableMap<String, Any>>(call.receiveText(),
                    object : TypeToken<MutableMap<String, Any>>() {}.type)
                val pro = call.sessions.get("DataBase")
                var data: MutableMap<String, String> = mutableMapOf()
                if (pro != null) {
                    data = Gson().fromJson<MutableMap<String, String>>(
                        if (pro.toString().isEmpty()) "{}" else (pro as Glitter.DataBase).data, object :
                            TypeToken<MutableMap<String, String>>() {}.type
                    )

                    if (data["SessionKey"] == null) {
                        val tok = (System.currentTimeMillis() + Random().nextInt(999999999)).toString() + ""
                        data["SessionKey"] = tok
                        call.sessions.set("DataBase", Glitter.DataBase(Gson().toJson(data)))
                    }
                    val file = File("dataBase/${data["SessionKey"]}")
                    if (!file.exists()) {
                        file.mkdir()
                    }
                    val sql =
                        if (data[jsonMap["dataBase"].toString()] != null) SqLiteConnector(data[jsonMap["dataBase"].toString()].toString())
                        else SqLiteConnector("dataBase/${data["SessionKey"]}/${jsonMap["dataBase"]}.db")
                    val rs = sql.stmt.executeQuery(jsonMap["text"].toString())
                    val rsMetaData: ResultSetMetaData = rs.metaData
                    while (rs.next()) {
                        val map: MutableMap<String, Any> = mutableMapOf()
                        for (i in 1 until rsMetaData.columnCount + 1) {
                            val name = rsMetaData.getColumnName(i)
                            map[name] = rs.getString(name)
                        }
                        returnMap.add(map)
                    }
                    sql.close()
                    call.respondText(Gson().toJson(returnMap))
                }
            }
            //執行DataBase
            post("/DataBase/exSql") {
                val returnMap: ArrayList<MutableMap<String, Any>> = ArrayList<MutableMap<String, Any>>()
                val jsonString = call.receiveText()
                println(jsonString)
                val jsonMap: MutableMap<String, Any> = Gson().fromJson<MutableMap<String, Any>>(jsonString,
                    object : TypeToken<MutableMap<String, Any>>() {}.type)
                val pro = call.sessions.get("DataBase")
                var data: MutableMap<String, String> = mutableMapOf()
                if (pro != null) {
                    data = Gson().fromJson<MutableMap<String, String>>(
                        if (pro.toString().isEmpty()) "{}" else (pro as Glitter.DataBase).data, object :
                            TypeToken<MutableMap<String, String>>() {}.type
                    )

                    if (data["SessionKey"] == null) {
                        val tok = (System.currentTimeMillis() + Random().nextInt(999999999)).toString() + ""
                        data["SessionKey"] = tok
                        call.sessions.set("DataBase", Glitter.DataBase(Gson().toJson(data)))
                    }
                    val file = File("dataBase/${data["SessionKey"]}")
                    if (!file.exists()) {
                        file.mkdir()
                    }
                    val sql =
                        if (data[jsonMap["dataBase"].toString()] != null) SqLiteConnector(data[jsonMap["dataBase"].toString()].toString())
                        else SqLiteConnector("dataBase/${data["SessionKey"]}/${jsonMap["dataBase"]}.db")
                    sql.stmt.executeUpdate(jsonMap["text"].toString())
                    sql.close()
                    call.respondText("true")
                }
            }
        }
    }
}

fun main() {
    ZipUtil.unzip(
        JzUtil.getRequest("https://storage.googleapis.com/squarestudio/Glitter_1.0.zip", 30 * 1000)!!.toByteArray()
            .inputStream(), "Glitter"
    )
}
