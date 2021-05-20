package glitterFrameWork

import glitterFrameWork.util.SqLiteConnector

class JavaScriptInterFace(val functionName:String,val function:(request:RequestFunction) -> Unit){
    companion object{
        //自定義JS執行函式
        val javaScriptInterFace: ArrayList<JavaScriptInterFace> = arrayListOf()
        fun addJavacScriptInterFace(myInterface: JavaScriptInterFace) {
            javaScriptInterFace.add(myInterface)
        }
        //添加官方函示庫
        init {
            //取紀錄
            addJavacScriptInterFace(JavaScriptInterFace("getPro"){
                    request->
                val uuid=request.receiveValue["uuid"]
                val name=request.receiveValue["name"]
                val sql= SqLiteConnector("dataBase/${uuid}.db")
                sql.stmt.executeUpdate("""CREATE TABLE   IF NOT EXISTS memory ( 
                       id   INTEGER PRIMARY KEY AUTOINCREMENT, 
                       name VARCHAR UNIQUE, 
                        data      VARCHAR);""")
                val rs = sql.stmt.executeQuery("select data from memory where name='$name'")
                while(rs.next()){
                    request.responseValue["data"]=rs.getString("data")
                }
                request.responseValue["result"]=true
            })
            //存紀錄
            addJavacScriptInterFace(JavaScriptInterFace("setPro"){
                    request->
                val uuid=request.receiveValue["uuid"]
                val name=request.receiveValue["name"]
                val data=request.receiveValue["data"]
                val sql= SqLiteConnector("dataBase/${uuid}.db")
                sql.stmt.executeUpdate("""  CREATE TABLE   IF NOT EXISTS `memory` ( 
                       id   INTEGER PRIMARY KEY AUTOINCREMENT, 
                       name VARCHAR UNIQUE, 
                        data      VARCHAR);""")
                sql.stmt.executeUpdate("insert or replace into memory (name,data)values('$name','$data')")
                request.responseValue["result"]=true
            })
        }
    }
}

class ApiInterFace(val rout:String,val functionName:String,val function:(request:RequestFunction) -> Unit){
    companion object{
    //自定義API函式
    val apiInterFace: ArrayList<ApiInterFace> = arrayListOf()
    fun addApiInterFace(myInterface: ApiInterFace) {
        apiInterFace.add(myInterface)
    }
    //DebugMode
    var deBugMode=false
    }
}

data class RequestFunction(val receiveValue:MutableMap<String,Any>, var responseValue:MutableMap<String,Any> = mutableMapOf())
