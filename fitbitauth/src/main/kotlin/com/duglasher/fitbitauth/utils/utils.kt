package com.duglasher.fitbitauth.utils

import com.duglasher.fitbitauth.exceptions.FitbitNotConfiguredException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


internal const val BASE_AUTH_URL = "https://www.fitbit.com/oauth2/authorize?response_type=code"
internal const val TOKEN_URL = "https://api.fitbit.com/oauth2/token"
internal const val REVOKE_URL = "https://api.fitbit.com/oauth2/revoke"


inline fun <T : Any> configDependent(crossinline configurableBlock: () -> Boolean) =
    object : ReadWriteProperty<Any?, T> {
        private var value: T? = null

        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            if (!configurableBlock()) {
                throw FitbitNotConfiguredException()
            }
            return value!!
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            this.value = value
        }
    }