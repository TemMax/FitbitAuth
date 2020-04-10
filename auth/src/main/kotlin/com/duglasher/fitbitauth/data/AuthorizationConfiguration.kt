package com.duglasher.fitbitauth.data

import androidx.collection.ArraySet
import com.duglasher.fitbitauth.Scope


class AuthorizationConfiguration internal constructor(
    internal val credentials: Credentials,
    internal val requiredScopes: ArraySet<Scope>,
    internal val scopes: ArraySet<Scope>,
    internal val expiresIn: Int
) {

    class Builder {

        private var credentials: Credentials? = null
        private var requiredScopes = ArraySet<Scope>()
        private var scopes = ArraySet<Scope>()
        private var expiresIn = ExpiresIn.WEEK

        fun setCredentials(
            clientId: String,
            clientSecret: String,
            redirectUrl: String
        ) = apply {
            require(clientId.isNotBlank()) { "client_id must not be blank" }
            require(clientId.isNotBlank()) { "client_secret must not be blank" }
            require(clientId.isNotBlank()) { "redirect_url must not be blank" }

            this.credentials = Credentials(
                clientId,
                clientSecret,
                redirectUrl
            )
        }

        fun addRequiredScopes(vararg scopes: Scope) = apply {
            this.scopes.addAll(scopes)
            this.requiredScopes.addAll(scopes)
        }

        fun addOptionalScopes(vararg scopes: Scope) = apply {
            this.scopes.addAll(scopes)
        }

        fun setExpiresIn(expiresIn: ExpiresIn) = apply {
            this.expiresIn = expiresIn
        }

        fun build(): AuthorizationConfiguration {
            requireNotNull(credentials)
            return AuthorizationConfiguration(
                credentials!!,
                requiredScopes,
                scopes,
                expiresIn.seconds
            )
        }

    }

    enum class ExpiresIn(val seconds: Int) {
        DAY(86400),
        WEEK(604800),
        MONTH(2592000),
        YEAR(31536000)
    }

}