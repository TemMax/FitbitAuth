package com.duglasher.fitbitauth

import com.duglasher.fitbitauth.api.ErrorFitbitResponse


sealed class FitbitAuthResult {
    object Success : FitbitAuthResult()
    class Error(val errors: List<ErrorFitbitResponse.Error>) : FitbitAuthResult()
    class Exception(val exception: Throwable) : FitbitAuthResult()
}