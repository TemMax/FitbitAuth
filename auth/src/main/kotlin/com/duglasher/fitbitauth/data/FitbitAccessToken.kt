package com.duglasher.fitbitauth.data

import androidx.collection.ArraySet
import com.duglasher.fitbitauth.Scope
import com.duglasher.fitbitauth.api.FitbitAccessTokenResponse
import com.google.gson.annotations.SerializedName


data class FitbitAccessToken internal constructor(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("scopes")
    val scopes: Set<Scope>,
    @SerializedName("expiration_date")
    val expirationDate: Long,
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("user_id")
    val userId: String
) {

    val isExpired: Boolean
        get() = expirationDate < System.currentTimeMillis() / 1000

    internal companion object {
        fun fromResponse(response: FitbitAccessTokenResponse): FitbitAccessToken {
            return with(response) {
                FitbitAccessToken(
                    accessToken,
                    refreshToken,
                    scope.split(' ').mapTo(ArraySet(), Scope::valueOf),
                    System.currentTimeMillis() + expiresIn,
                    tokenType,
                    userId
                )
            }
        }
    }

}