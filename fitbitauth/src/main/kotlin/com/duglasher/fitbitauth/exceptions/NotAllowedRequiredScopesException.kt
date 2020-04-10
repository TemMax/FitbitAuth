package com.duglasher.fitbitauth.exceptions

import com.duglasher.fitbitauth.Scope


@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")
class NotAllowedRequiredScopesException(val deniedScopes: List<Scope>) :
    Exception("Not allowed scopes: ${deniedScopes.joinToString()}")