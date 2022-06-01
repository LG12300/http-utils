@file:JvmName("OkHttpUtils")

package cn.senseless.http

import com.google.gson.Gson
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.FormBody
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.InputStream

val gson = Gson()

private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.NONE
    })
    .build()

class Http internal constructor(private val url: String) {

    private val builder = Request.Builder()

    fun addHeaders(vararg headers: Pair<String, String>): Http {
        headers.forEach {
            builder.addHeader(it.first, it.second)
        }
        return this
    }

    private fun getCall(request: Request): Call {
        return okHttpClient.newCall(request)
    }

    private suspend fun Call.await(): String {
        return suspendCancellableCoroutine {
            it.invokeOnCancellation { cancel() }
            execute().use { response ->
                if (response.isSuccessful) {
                    it.resumeWith(Result.success(response.body!!.string()))
                } else {
                    it.resumeWith(Result.failure(RuntimeException("${response.code}: ${response.message} ${response.body?.string()}")))
                }
            }
        }
    }

    suspend fun get(): String {
        return getCall(builder.url(url).get().build()).await()
    }

    suspend fun download(): InputStream {
        val call = getCall(builder.url(url).get().build())
        call.execute().use {
            return it.body!!.byteStream()
        }
    }

    suspend fun post(vararg params: Pair<String, Any>): String {
        val body = FormBody.Builder()
        params.forEach {
            body.add(it.first, it.second.toString())
        }
        return getCall(builder.url(url).post(body.build()).build()).await()
    }

    suspend fun html(): String {
        return getCall(builder.url(url).get().build()).await()
    }
}

fun http(url: String) = Http(url)

//inline fun <reified T> String.json(): T {
//    return gson.fromJson(this, T::class.java)
//}
