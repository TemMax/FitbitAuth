package com.duglasher.fitbitauth.api

import android.util.Base64
import com.duglasher.fitbitauth.data.AuthorizationConfiguration
import com.duglasher.fitbitauth.data.Credentials
import com.duglasher.fitbitauth.data.FitbitAccessToken
import com.duglasher.fitbitauth.utils.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


internal class FitbitApi(
    private val client: OkHttpClient
) {

    suspend fun requestAccessToken(
        authCode: String,
        authConfig: AuthorizationConfiguration
    ): FitbitApiResult<FitbitAccessToken> =
        requestToken(
            authConfig,
            createFormBodyOf(
                "grant_type" to "authorization_code",
                "code" to authCode,
                "client_id" to authConfig.credentials.clientId,
                "redirect_uri" to authConfig.credentials.redirectUrl,
                "expires_in" to authConfig.expiresIn.toString()
            )
        )

    suspend fun refreshToken(
        refreshToken: String,
        authConfig: AuthorizationConfiguration
    ): FitbitApiResult<FitbitAccessToken> =
        requestToken(
            authConfig,
            createFormBodyOf(
                "grant_type" to "refresh_token",
                "refresh_token" to refreshToken,
                "expires_in" to authConfig.expiresIn.toString()
            )
        )

    fun logout(accessToken: String, authConfig: AuthorizationConfiguration) {
        val body = createFormBodyOf("token" to accessToken)

        val request = createRequest(REVOKE_URL, authConfig.credentials, body)

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = Unit
            override fun onResponse(call: Call, response: Response) = Unit
        })
    }

    private suspend fun requestToken(authConfig: AuthorizationConfiguration, body: FormBody): FitbitApiResult<FitbitAccessToken> {
        return client.newCall(createRequest(TOKEN_URL, authConfig.credentials, body)).awaitResult {
            val json = JSONObject(it)
            json.put("expiration_date", System.currentTimeMillis() + json.getInt("expires_in"))
            json.put("scopes", JSONArray(json.getString("scope").split(' ')))
            FitbitAccessToken(json)
        }
    }

    private fun createFormBodyOf(vararg params: Pair<String, String>): FormBody {
        return FormBody.Builder(Charsets.UTF_8).apply {
            for ((name, value) in params) {
                add(name, value)
            }
        }.build()
    }

    private fun createRequest(url: String, credentials: Credentials, body: FormBody): Request {
        val encodedHeader =
            Base64.encodeToString("${credentials.clientId}:${credentials.clientSecret}".toByteArray(), Base64.NO_WRAP)

        return Request.Builder().apply {
            url(url)
            addHeader("Authorization", "Basic $encodedHeader")
            post(body)
        }.build()
    }

    internal companion object {
        internal const val BASE_AUTH_URL = "https://www.fitbit.com/oauth2/authorize?response_type=code"
        private const val TOKEN_URL = "https://api.fitbit.com/oauth2/token"
        private const val REVOKE_URL = "https://api.fitbit.com/oauth2/revoke"
    }

}