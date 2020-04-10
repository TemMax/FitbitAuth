package com.duglasher.fitbitauth


interface FitbitAuthHandler {
    fun onAuthResult(authResult: FitbitAuthResult)
}