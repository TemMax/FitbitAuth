package com.duglasher.fitbitauth

import android.app.Application
import com.duglasher.fitbitauth.data.AuthorizationConfiguration


class App : Application() {

	override fun onCreate() {
		super.onCreate()
		FitbitAuthManager.configure(
			this,
			AuthorizationConfiguration.Builder()
				.setCredentials(
					BuildConfig.CLIENT_ID,
					BuildConfig.CLIENT_SECRET,
					BuildConfig.REDIRECT_URL
				)
				.addRequiredScopes(Scope.nutrition, Scope.profile)
				.setExpiresIn(AuthorizationConfiguration.ExpiresIn.WEEK)
				.build()
		)
	}

}