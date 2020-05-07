package com.duglasher.fitbitauth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.duglasher.fitbitauth.api.ErrorFitbitResponse
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), FitbitAuthHandler {

	private lateinit var fitBitAuth: FitbitAuthManager
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		fitBitAuth = (application as App).fitBitAuth

		btnLogin.setOnClickListener {
			if (fitBitAuth.isLoggedIn()) {
				fitBitAuth.logout()
				btnLogin.text = "login"
			} else {
				fitBitAuth.login(this)
			}
		}

		updateButton()
	}

	private fun updateButton() {
		btnLogin.text = if (fitBitAuth.isLoggedIn()) "logout" else "login"
	}

	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)
		if (intent != null) {
			fitBitAuth.handleIntent(this, intent)
		}
	}

	override fun onAuthResult(authResult: FitbitAuthResult) {
		when (authResult) {
			FitbitAuthResult.Success      -> {
				Log.d(
					"FitbitAuth",
					"Login success! Scopes: ${fitBitAuth.getAccessToken().scopes.joinToString()}"
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