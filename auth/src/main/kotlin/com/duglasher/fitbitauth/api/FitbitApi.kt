package com.duglasher.fitbitauth.api

import com.duglasher.fitbitauth.data.AuthorizationConfiguration
import com.duglasher.fitbitauth.data.Credentials
import com.duglasher.fitbitauth.utils.*
import okhttp3.*
import java.io.IOException


internal object FitbitApi {

	var client = OkHttpClientCreator.create()

	suspend fun requestAccessToken(
		authCode: String,
		authConfig: AuthorizationConfiguration
	): FitbitApiResult<FitbitAccessTokenResponse> {
		val body = createFormBodyOf(
			"grant_type" to "authorization_code",
			"code" to authCode,
			"client_id" to authConfig.credentials.clientId,
			"redirect_uri" to authConfig.credentials.redirectUrl,
			"expires_in" to authConfig.expiresIn.toString()
		)

		val request = createRequest(TOKEN_URL, authConfig.credentials, body)

		return client.newCall(request).awaitResult(GsonHelper::fromJson)
	}

	suspend fun refreshToken(
		refreshToken: String,
		authConfig: AuthorizationConfiguration
	): FitbitApiResult<FitbitAccessTokenResponse> {
		val body = createFormBodyOf(
			"grant_type" to "refresh_token",
			"refresh_token" to refreshToken,
			"expires_in" to authConfig.expiresIn.toString()
		)

		val request = createRequest(TOKEN_URL, authConfig.credentials, body)

		return client.newCall(request).awaitResult(GsonHelper::fromJson)
	}

	fun logout(accessToken: String, authConfig: AuthorizationConfiguration) {
		val body = createFormBodyOf("token" to accessToken)

		val request = createRequest(REVOKE_URL, authConfig.credentials, body)

		client.newCall(request).enqueue(object : Callback {
			override fun onFailure(call: Call, e: IOException) = Unit
			override fun onResponse(call: Call, response: Response) = Unit
		})
	}

	private fun createFormBodyOf(vararg params: Pair<String, String>): FormBody {
		return FormBody.Builder(Charsets.UTF_8).apply {
			for ((name, value) in params) {
				add(name, value)
			}
		}.build()
	}

	private fun createRequest(url: String, credentials: Credentials, body: FormBody): Request {
		val encodedHeader = "${credentials.clientId}:${credentials.clientSecret}".toBase64()

		return Request.Builder().apply {
			url(url)
			addHeader("Authorization", "Basic $encodedHeader")
			post(body)
		}.build()
	}

}