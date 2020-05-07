package com.duglasher.fitbitauth.utils

import com.duglasher.fitbitauth.api.ErrorFitbitResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resumeWithException


internal sealed class FitbitApiResult<out T> {
    class Success<out T>(val value: T) : FitbitApiResult<T>()
    class Error(val error: ErrorFitbitResponse) : FitbitApiResult<Nothing>()
    class Exception(val exception: Throwable) : FitbitApiResult<Nothing>()
}

internal suspend fun <T> Call.awaitResult(converter: (String) -> T): FitbitApiResult<T> {
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                continuation.resumeWith(runCatching {
                    val body: ResponseBody? = response.body
                    if (body != null) {
                        if (response.isSuccessful) {
                            FitbitApiResult.Success(converter(body.string()))
                        } else {
                            FitbitApiResult.Error(ErrorFitbitResponse(JSONObject(body.string())))
                        }
                    } else {
                        FitbitApiResult.Exception(Exception("Response body is null"))
                    }
                })
            }

            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isCancelled) return
                continuation.resumeWithException(e)
            }
        })

        continuation.invokeOnCancellation {
            try {
                cancel()
            } catch (ex: Throwable) {
            }
        }
    }
}