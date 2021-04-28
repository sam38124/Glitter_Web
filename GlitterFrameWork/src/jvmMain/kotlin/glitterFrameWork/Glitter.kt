package glitterFrameWork

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import glitterFrameWork.util.File_Refresh
import glitterFrameWork.util.SqLiteConnector
import glitterFrameWork.util.ZipUtil
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
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
import java.sql.ResultSetMetaData
import java.util.*
import kotlin.collections.ArrayList

object Glitter {
    data class ProData(var data:String)
    data class DataBase(var data:String)
    fun setUp(rout:Routing,session:(Sessions.Configuration)->Unit){
        val glitterRout=File("Glitter")
        if(!glitterRout.exists()){
            glitterRout.mkdir()
        }
        val dataBaseRout=File("dataBase")
        if(!dataBaseRout.exists()){
            dataBaseRout.mkdir()
        }
        rout{
            install(Sessions) {
                cookie<ProData>("Pro")
                cookie<DataBase>("DataBase")
                session(this)
            }
            //取得Glitter版本號
            get("/GlitterVersion") {
                call.respondText(File("Glitter/${call.parameters["AppName"]}/version").readText())
            }
            //下載Glitter
            get("/GetGlitter") {
                val nowVersion = File("Glitter/${call.parameters["AppName"]}/version").readText()
                while(ZipUtil.UnZip){Thread.sleep(1000)}
                var versionRout= File("Glitter/${call.parameters["AppName"]}/VersionHistory")
                if(!versionRout.exists()){
                    versionRout.mkdir()
                }
                if (!File("$versionRout/${nowVersion}.zip").exists()) {
                    println("unzip")
                    ZipUtil.unzip("Glitter/${call.parameters["AppName"]}",nowVersion)
                }
                val file= File("$versionRout/${call.parameters["version"]}.zip")
                if(file.exists()){
                    call.respondFile(File("$versionRout/${call.parameters["version"]}.zip"))
                }else{
                    call.respondText("nodata")
                }
            }
            //存紀錄
            post("/setPro"){
                val pro=call.sessions.get("Pro")
                var data:MutableMap<String,String> = mutableMapOf()
                if(pro!=null){
                    data= Gson().fromJson<MutableMap<String,String>>(if(pro.toString().isEmpty()) "{}" else (pro as ProData).data,object :
                        TypeToken<MutableMap<String, String>>(){}.type)
                }
                data[call.parameters["key"]!!]=call.receiveText()
                call.sessions.set("Pro",ProData(Gson().toJson(data)))
                println("setPro--${call.sessions.get("Pro")}")
                call.respondText("true")
            }
            //拿紀錄
            post("/getPro"){
                val pro=call.sessions.get("Pro")
                var data:MutableMap<String,String> = mutableMapOf()
                if(pro!=null){
                    data= Gson().fromJson<MutableMap<String,String>>(if(pro.toString().isEmpty()) "{}" else (pro as ProData).data,object :
                        TypeToken<MutableMap<String, String>>(){}.type)
                    println("getPro--"+Gson().toJson(data))
                }
                call.respondText(Gson().toJson(data))
            }
            //文件更新
            File_Refresh.fileRefresh(rout, arrayOf("Glitter"))
            //取得DataBase
            post("/DataBase/initByFile"){
                val jsonMap:MutableMap<String,Any> = Gson().fromJson<MutableMap<String,Any>>(call.receiveText(),object :TypeToken<MutableMap<String,Any>>(){}.type)
                val pro=call.sessions.get("DataBase")
                var data:MutableMap<String,String> = mutableMapOf()
                if(pro!=null){
                    data= Gson().fromJson<MutableMap<String,String>>(if(pro.toString().isEmpty()) "{}" else (pro as Glitter.DataBase).data,object :
                        TypeToken<MutableMap<String, String>>(){}.type)
                }
                data[jsonMap["dataBase"].toString()]=jsonMap["rout"].toString()
                call.sessions.set("DataBase", Glitter.DataBase(Gson().toJson(data)))
                println("setDataBase--${call.sessions.get("DataBase")}")
                call.respondText("true")
            }
            //查詢DataBase
            post("/DataBase/Query"){
                val returnMap:ArrayList<MutableMap<String,Any>> = ArrayList<MutableMap<String,Any>>()
                val jsonMap:MutableMap<String,Any> = Gson().fromJson<MutableMap<String,Any>>(call.receiveText(),object :TypeToken<MutableMap<String,Any>>(){}.type)
                val pro=call.sessions.get("DataBase")
                var data:MutableMap<String,String> = mutableMapOf()
                if(pro!=null){
                    data= Gson().fromJson<MutableMap<String,String>>(if(pro.toString().isEmpty()) "{}" else (pro as Glitter.DataBase).data,object :
                        TypeToken<MutableMap<String, String>>(){}.type)

                    if(data["SessionKey"]==null){
                        val tok = (System.currentTimeMillis() + Random().nextInt(999999999)).toString() + ""
                        data["SessionKey"]=tok
                        call.sessions.set("DataBase", Glitter.DataBase(Gson().toJson(data)))
                    }
                    var file=File("dataBase/${data["SessionKey"]}")
                    if(!file.exists()){file.mkdir()}
                    val sql=
                        if(data[jsonMap["dataBase"].toString()]!=null) SqLiteConnector(data[jsonMap["dataBase"].toString()].toString())
                        else SqLiteConnector("dataBase/${data["SessionKey"]}/${jsonMap["dataBase"]}.db")
                    val rs=sql.stmt.executeQuery(jsonMap["text"].toString())
                    val rsMetaData: ResultSetMetaData = rs.metaData
                    while(rs.next()){
                        val map:MutableMap<String,Any> = mutableMapOf()
                        for(i in 1 until rsMetaData.columnCount+1){
                            val name=rsMetaData.getColumnName(i)
                            map[name]=rs.getString(name)
                        }
                        returnMap.add(map)
                    }
                    sql.close()
                    call.respondText(Gson().toJson(returnMap))
                }
            }
            //執行DataBase
            post("/DataBase/exSql"){
                val returnMap:ArrayList<MutableMap<String,Any>> = ArrayList<MutableMap<String,Any>>()
                val jsonString=call.receiveText()
                println(jsonString)
                val jsonMap:MutableMap<String,Any> = Gson().fromJson<MutableMap<String,Any>>(jsonString,object :TypeToken<MutableMap<String,Any>>(){}.type)
                val pro=call.sessions.get("DataBase")
                var data:MutableMap<String,String> = mutableMapOf()
                if(pro!=null){
                    data= Gson().fromJson<MutableMap<String,String>>(if(pro.toString().isEmpty()) "{}" else (pro as Glitter.DataBase).data,object :
                        TypeToken<MutableMap<String, String>>(){}.type)

                    if(data["SessionKey"]==null){
                        val tok = (System.currentTimeMillis() + Random().nextInt(999999999)).toString() + ""
                        data["SessionKey"]=tok
                        call.sessions.set("DataBase", Glitter.DataBase(Gson().toJson(data)))
                    }
                    val file=File("dataBase/${data["SessionKey"]}")
                    if(!file.exists()){file.mkdir()}
                    val sql=
                        if(data[jsonMap["dataBase"].toString()]!=null) SqLiteConnector(data[jsonMap["dataBase"].toString()].toString())
                        else SqLiteConnector("dataBase/${data["SessionKey"]}/${jsonMap["dataBase"]}.db")
                    sql.stmt.executeUpdate(jsonMap["text"].toString())
                    sql.close()
                    call.respondText("true")
                }
            }
        }
    }
}