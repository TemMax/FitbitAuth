package com.duglasher.fitbitauth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.duglasher.fitbitauth.api.ErrorFitbitResponse
import com.duglasher.fitbitauth.api.FitbitApi
import com.duglasher.fitbitauth.data.AuthorizationConfiguration
import com.duglasher.fitbitauth.exceptions.NotAllowedRequiredScopesException
import com.duglasher.fitbitauth.utils.BASE_AUTH_URL
import com.duglasher.fitbitauth.utils.FitbitApiResult
import com.duglasher.fitbitauth.utils.Prefs
import com.duglasher.fitbitauth.utils.configDependent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import saschpe.android.customtabs.CustomTabsHelper
import kotlin.random.Random


object FitbitAuthManager {

	private val state: String = Random.nextInt(10000, 99999).toString()

	private var customTabsIntent = CustomTabsIntent.Builder()
		.setToolbarColor(0xFFFEFEFE.toInt())
		.setShowTitle(true)
		.build()

	private var configured = false

	private var prefs: Prefs by configDependent { configured }
	private var accountManager: AccountManager by configDependent { configured }
	private var authConfig: AuthorizationConfiguration by configDependent { configured }

	fun configure(appContext: Context, authConfig: AuthorizationConfiguration) {
		this.configured = true

		this.prefs = Prefs(appContext)
		this.accountManager = AccountManager(prefs)
		this.authConfig = authConfig
	}

	fun setCustomTabsIntent(intent: CustomTabsIntent) {
		this.customTabsIntent = intent
	}

	fun setOkHttpClient(client: OkHttpClient) {
		FitbitApi.client = client
	}

	@Synchronized
	fun getAccessToken() = accountManager.get()

	@Synchronized
	fun isLoggedIn() = accountManager.isLoggedIn()

	@Synchronized
	fun login(activity: Activity) {
		val authUri = Uri.parse(BASE_AUTH_URL).buildUpon().apply {
			appendQueryParameter("client_id", authConfig.credentials.clientId)
			appendQueryParameter("redirect_uri", authConfig.credentials.redirectUrl)
			if (authConfig.scopes.isNotEmpty()) {
				appendQueryParameter("scope", authConfig.scopes.joinToString(" "))
			}
			appendQueryParameter("expires_in", authConfig.expiresIn.toString())
			appendQueryParameter("state", state)
		}.build()



		CustomTabsHelper.openCustomTab(
			activity,
			customTabsIntent,
			authUri,
			object : CustomTabsHelper.CustomTabFallback {
				override fun openUri(context: Context?, uri: Uri?) {
					val intent = Intent(Intent.ACTION_VIEW)
					intent.data = authUri
					activity.startActivity(intent)
				}
			})
	}

	@Synchronized
	suspend fun refreshToken(): FitbitAuthResult {
		val tokenResult = withContext(Dispatchers.IO) {
			FitbitApi.refreshToken(accountManager.get().refreshToken, authConfig)
		}
		return when (tokenResult) {
			is FitbitApiResult.Success   -> {
				accountManager.save(tokenResult.value)
				FitbitAuthResult.Success
			}
			is FitbitApiResult.Error     -> FitbitAuthResult.Error(tokenResult.error.errors)
			is FitbitApiResult.Exception -> FitbitAuthResult.Exception(tokenResult.exception)
		}
	}

	@Synchronized
	fun logout() {
		FitbitApi.logout(accountManager.get().accessToken, authConfig)
		accountManager.logout()
	}

	@Synchronized
	fun <A> handleIntent(
		activity: A,
		intent: Intent
	) where A : LifecycleOwner, A : FitbitAuthHandler {
		intent.data?.let { authUri ->
			val redirectUri = "${authUri.scheme}://${authUri.host}"
			val authState = authUri.getQueryParameter("state") ?: ""
			if (redirectUri.equals(
					authConfig.credentials.redirectUrl,
					true
				) && authState == state
			) {
				val error = authUri.getQueryParameter("error")
				val errorDescription = authUri.getQueryParameter("error_description") ?: ""
				if (error == null) {
					authUser(activity, authUri.getQueryParameter("code")!!)
				} else {
					activity.onAuthResult(
						FitbitAuthResult.Error(
							listOf(
								ErrorFitbitResponse.Error(
									error,
									errorDescription
								)
							)
						)
					)
				}
			}
		}
	}

	private fun <A> authUser(
		activity: A,
		authCode: String
	) where A : LifecycleOwner, A : FitbitAuthHandler {
		activity.lifecycleScope.launch {
			val tokenResult = withContext(Dispatchers.IO) {
				FitbitApi.requestAccessToken(authCode, authConfig)
			}
			when (tokenResult) {
				is FitbitApiResult.Success   -> {
					val allowedScopes = tokenResult.value.scope.split(" ").map(Scope::valueOf)
					val deniedRequiredScopes = authConfig.scopes subtract allowedScopes
					if (deniedRequiredScopes.isEmpty()) {
						accountManager.save(tokenResult.value)
						activity.onAuthResult(FitbitAuthResult.Success)
					} else {
						activity.onAuthResult(
							FitbitAuthResult.Exception(
								NotAllowedRequiredScopesException(
									deniedRequiredScopes.toList()
								)
							)
						)
					}
				}
				is FitbitApiResult.Error     -> activity.onAuthResult(
					FitbitAuthResult.Error(
						tokenResult.error.errors
					)
				)
				is FitbitApiResult.Exception -> activity.onAuthResult(
					FitbitAuthResult.Exception(
						tokenResult.exception
					)
				)
			}
		}
	}

}