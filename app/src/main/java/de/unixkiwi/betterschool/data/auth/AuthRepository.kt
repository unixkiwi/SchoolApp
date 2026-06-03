package de.unixkiwi.betterschool.data.auth

import android.content.Intent
import androidx.core.net.toUri
import de.unixkiwi.betterschool.core.AUTHORIZE_URI
import de.unixkiwi.betterschool.core.CLIENT_ID
import de.unixkiwi.betterschool.core.REDIRECT_URI
import de.unixkiwi.betterschool.core.TOKEN_URI
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.CodeVerifierUtil
import net.openid.appauth.ResponseTypeValues
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRepository(
    private val authService: AuthorizationService,
    private val localTokenSource: LocalTokenSource
) {
    companion object {
        private const val TAG = "AuthRepo"
    }

    private val config = AuthorizationServiceConfiguration(
        AUTHORIZE_URI.toUri(),
        TOKEN_URI.toUri()
    )

    fun createAuthRequestIntent(): Intent {
        val codeVerifier = CodeVerifierUtil.generateRandomCodeVerifier()

        return authService.getAuthorizationRequestIntent(
            AuthorizationRequest.Builder(
                config,
                CLIENT_ID,
                ResponseTypeValues.CODE,
                REDIRECT_URI.toUri()
            ).setCodeVerifier(codeVerifier).build()
        )
    }

    suspend fun isTokenLocally(): Boolean {
        Timber.tag(TAG).d("isTokenLocally called!")
        val token: String? = localTokenSource.getToken()
        Timber.tag(TAG).d("called getToken!")
        return !token.isNullOrEmpty()
    }

    suspend fun getToken(): String? {
        Timber.tag(TAG).d("getToken() called!")
        return localTokenSource.getToken()
    }

    suspend fun getAuthHeader(): String? {
        val token = getToken()
        if (token.isNullOrBlank()) return null
        return if (token.startsWith("Bearer ")) token else "Bearer $token"
    }

    suspend fun clearToken() {
        Timber.tag(TAG).d("clearToken() called")
        localTokenSource.clearToken()
        Timber.tag(TAG).w("Cleared token!")
    }

    suspend fun isTokenExpired(): Boolean {
        return localTokenSource.isTokenExpired()
    }

    suspend fun getTokenFromAuthResponse(authResponse: AuthorizationResponse) {
        val tokenData = suspendCancellableCoroutine { cont ->
            val tokenRequest = authResponse.createTokenExchangeRequest()

            authService.performTokenRequest(tokenRequest) { res, ex ->
                if (!cont.isActive) return@performTokenRequest

                when {
                    ex != null -> {
                        Timber.tag(TAG).e("Msg: $ex")
                        cont.resumeWithException(ex)
                    }

                    res?.accessToken != null -> {
                        val expiryTime = res.accessTokenExpirationTime
                        Timber.tag(TAG).d("Token received with expiry time: $expiryTime")
                        cont.resume(Pair(res.accessToken!!, expiryTime))
                    }

                    else -> cont.resumeWithException(IllegalStateException("No access token"))
                }
            }
        }

        localTokenSource.setToken(tokenData.first, tokenData.second)
    }
}