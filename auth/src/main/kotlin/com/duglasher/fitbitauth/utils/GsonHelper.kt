package com.duglasher.fitbitauth.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Reader


internal object GsonHelper {
    val gson = Gson()

    inline fun <reified T> fromJson(json: String): T =
        gson.fromJson<T>(json, object : TypeToken<T>() {}.type)

    inline fun <reified T> fromJson(reader: Reader): T =
        gson.fromJson<T>(reader, object : TypeToken<T>() {}.type)

    inline fun <reified T> toJson(value: T): String =
        gson.toJson(value, object : TypeToken<T>() {}.type)
}