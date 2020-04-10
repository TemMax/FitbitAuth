package com.duglasher.fitbitauth.api

import com.google.gson.annotations.SerializedName


class ErrorFitbitResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("errors")
    val errors: List<Error>
) {

    class Error(
        @SerializedName("errorType")
        val errorType: String,
        @SerializedName("message")
        val message: String
    )

}