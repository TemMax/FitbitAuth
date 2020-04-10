package com.duglasher.fitbitauth.exceptions


class FitbitNotConfiguredException : Exception(
    "Fitbit must be configured with FitbitAuthManager#configure() in your Application class!"
)