package com.duglasher.fitbitauth.data

import com.duglasher.fitbitauth.Scope
import com.duglasher.fitbitauth.utils.mapStringsTo
import org.json.JSONObject
import java.util.*


data class FitbitAccessToken internal constructor(
    private val json: JSONObject
) {

    val accessToken: String get() = json.optString("access_token", null)
    val refreshToken: String get() = json.optString("refresh_token", null)
    val scopes: Set<Scope> get() = json.optJSONArray("scopes").mapStringsTo(EnumSet.noneOf(Scope::class.java), Scope::valueOf)
    val expirationDate: Long get() = json.optLong("expiration_date", 0L)
    val tokenType: String get() = json.optString("token_type", null)
    val userId: String get() = json.optString("user_id", null)

    val isExpired: Boolean
        get() = expirationDate < System.currentTimeMillis() / 1000

    fun toJson(): String = json.toString()

}