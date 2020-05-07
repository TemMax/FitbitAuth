package com.duglasher.fitbitauth.api

import com.duglasher.fitbitauth.utils.mapObjsTo
import org.json.JSONObject


class ErrorFitbitResponse(
    private val json: JSONObject
) {
    val success: Boolean get() = json.getBoolean("success")
    val errors: List<Error> get() = json.getJSONArray("errors").mapObjsTo(ArrayList(), ::Error)

    class Error(
        val errorType: String,
        val message: String
    ) {
        constructor(json: JSONObject) : this(
            json.optString("errorType", null),
            json.optString("message", null)
        )

    }

}