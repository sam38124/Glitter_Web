package glitterFrameWork

class JavaScriptInterFace(val functionName:String,val function:(request:RequestFunction) -> Unit){
    companion object{
        //自定義JS執行函式
        val javaScriptInterFace: ArrayList<JavaScriptInterFace> = arrayListOf()
        fun addJavacScriptInterFace(myInterface: JavaScriptInterFace) {
            javaScriptInterFace.add(myInterface)
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
