package com.duglasher.fitbitauth

import com.duglasher.fitbitauth.api.FitbitAccessTokenResponse
import com.duglasher.fitbitauth.data.FitbitAccessToken
import com.duglasher.fitbitauth.exceptions.FitbitNotAuthenticatedException
import com.duglasher.fitbitauth.utils.Prefs


internal class AccountManager(private val prefs: Prefs) {

	private companion object {
		private var token: FitbitAccessToken? = null
	}

	fun get(): FitbitAccessToken {
		if (token == null) {
			token = prefs.getAccessToken()
		}
		return token!!
	}

	fun isLoggedIn(): Boolean {
		return try {
			!get().isExpired
		} catch (e: FitbitNotAuthenticatedException) {
			false
		}
	}

	fun save(response: FitbitAccessTokenResponse) {
		val accessToken = FitbitAccessToken.fromResponse(response)
		prefs.saveAccessToken(accessToken)

		token = accessToken
	}

	fun logout() {
		prefs.deleteAccessToken()
		token = null
	}

}