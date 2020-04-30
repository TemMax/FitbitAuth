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
import com.duglasher.fitbitauth.utils.FitbitApiResult
import com.duglasher.fitbitauth.utils.OkHttpClientCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import saschpe.android.customtabs.CustomTabsHelper
import kotlin.random.Random


class FitbitAuthManager(
    appContext: Context,
    private val authConfig: AuthorizationConfiguration,
    private val customTabsIntent: CustomTabsIntent = CustomTabsIntent.Builder()
        .setToolbarColor(0xFFFEFEFE.toInt())
        .setShowTitle(true)
        .build(),
    okHttpClient: OkHttpClient = OkHttpClientCreator.create()
) {
    
    private val api = FitbitApi(okHttpClient)

    private val state: String = Random.nextInt(10000, 99999).toString()

    private val accountManager: AccountManager = AccountManager(appContext)

    fun getAccessToken() = accountManager.get()

    fun isLoggedIn() = accountManager.isLoggedIn()

    fun login(activity: Activity) {
        val authUri = Uri.parse(FitbitApi.BASE_AUTH_URL).buildUpon().apply {
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
            api.refreshToken(accountManager.get().refreshToken, authConfig)
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
        api.logout(accountManager.get().accessToken, authConfig)
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
                    authUserLocked(activity, authUri.getQueryParameter("code")!!)
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

    private fun <A> authUserLocked(
        activity: A,
        authCode: String
    ) where A : LifecycleOwner, A : FitbitAuthHandler {
        activity.lifecycleScope.launch {
            val tokenResult = withContext(Dispatchers.IO) {
                api.requestAccessToken(authCode, authConfig)
            }
            when (tokenResult) {
                is FitbitApiResult.Success   -> {
                    val allowedScopes = tokenResult.value.scopes
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