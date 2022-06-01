import cn.senseless.http.http
import kotlinx.coroutines.*
import okhttp3.internal.wait

fun main() {
    val coroutineScope = CoroutineScope(Dispatchers.Default)
    coroutineScope.launch {
        println("baidu start")
        val result = http("https://www.baidu.com")
            .addHeaders("" to "")
            .get()
        println("baidu end")
    }
    coroutineScope.launch {
        println("bing start")
        val result = http("https://www.bing.com").get()
        println("bing end")
    }
    runBlocking { coroutineScope.coroutineContext.job.join() }
}