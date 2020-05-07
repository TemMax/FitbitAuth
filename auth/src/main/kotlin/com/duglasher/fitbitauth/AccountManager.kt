package com.duglasher.fitbitauth

import android.content.Context
import com.duglasher.fitbitauth.data.FitbitAccessToken
import com.duglasher.fitbitauth.exceptions.FitbitNotAuthenticatedException
import org.json.JSONObject


internal class AccountManager(context: Context) {

    private val prefs = context.getSharedPreferences("app_fitbit_prefs", Context.MODE_PRIVATE)

    fun get(): FitbitAccessToken {
        val tokenJson = prefs.getString(ACCESS_TOKEN_KEY, "")!!
        if (tokenJson.isBlank()) {
                throw FitbitNotAuthenticatedException()
            }
        return FitbitAccessToken(JSONObject(tokenJson))
    }

    fun isLoggedIn(): Boolean {
        return try {
            !get().isExpired
        } catch (e: FitbitNotAuthenticatedException) {
            false
        }
    }

    fun save(token: FitbitAccessToken) {
        prefs.edit().putString(ACCESS_TOKEN_KEY, token.toJson()).apply()
    }

    fun logout() {
        prefs.edit().remove(ACCESS_TOKEN_KEY).apply()
    }

    private companion object {
        private const val ACCESS_TOKEN_KEY = "app_fitbit_prefs.access_token"
    }

}