package com.duglasher.fitbitauth.utils

import android.content.Context
import com.duglasher.fitbitauth.data.FitbitAccessToken
import com.duglasher.fitbitauth.exceptions.FitbitNotAuthenticatedException


internal class Prefs(appContext: Context) {

    private val sp = appContext.getSharedPreferences("app_fitbit_prefs", Context.MODE_PRIVATE)

    fun getAccessToken(): FitbitAccessToken {
        val tokenJson = sp.getString(ACCESS_TOKEN_KEY, "")!!
        if (tokenJson.isBlank()) {
            throw FitbitNotAuthenticatedException()
        }
        return GsonHelper.fromJson(tokenJson)
    }

    fun saveAccessToken(accessToken: FitbitAccessToken) {
        sp.edit().putString(ACCESS_TOKEN_KEY, GsonHelper.toJson(accessToken)).apply()
    }

    fun deleteAccessToken() {
        sp.edit().remove(ACCESS_TOKEN_KEY).apply()
    }

    private companion object {
        private const val ACCESS_TOKEN_KEY = "app_fitbit_prefs.access_token"
    }

}