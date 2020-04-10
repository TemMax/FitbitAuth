package com.duglasher.fitbitauth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.duglasher.fitbitauth.api.ErrorFitbitResponse
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), FitbitAuthHandler {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		btnLogin.setOnClickListener {
			if (FitbitAuthManager.isLoggedIn()) {
				FitbitAuthManager.logout()
				btnLogin.text = "login"
			} else {
				FitbitAuthManager.login(this)
			}
		}

		updateButton()
	}

	private fun updateButton() {
		btnLogin.text = if (FitbitAuthManager.isLoggedIn()) "logout" else "login"
	}

	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)
		if (intent != null) {
			FitbitAuthManager.handleIntent(this, intent)
		}
	}

	override fun onAuthResult(authResult: FitbitAuthResult) {
		when (authResult) {
			FitbitAuthResult.Success      -> {
				Log.d(
					"FitbitAuth",
					"Login success! Scopes: ${FitbitAuthManager.getAccessToken().scopes.joinToString()}"
				)
			}
			is FitbitAuthResult.Error     -> {
				Log.d(
					"FitbitAuth",
					"Login error!\nErrors: ${authResult.errors.map(ErrorFitbitResponse.Error::errorType).joinToString()}"
				)
			}
			is FitbitAuthResult.Exception -> {
				Log.d("FitbitAuth", "Login exception!")
				authResult.exception.printStackTrace()
			}
		}

		updateButton()
	}

}