package de.unixkiwi.betterschool.data.auth

import android.content.Intent
import androidx.core.net.toUri
import de.unixkiwi.betterschool.core.AUTHORIZE_URI
import de.unixkiwi.betterschool.core.CLIENT_ID
import de.unixkiwi.betterschool.core.REDIRECT_URI
import de.unixkiwi.betterschool.core.TOKEN_URI
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.CodeVerifierUtil
import net.openid.appauth.GrantTypeValues
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenRequest
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

    private val refreshMutex = Mutex()

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
        if (isTokenExpired()) {
            refreshMutex.withLock {
                Timber.tag(TAG).d("Token expired, attempting to refresh")
                refreshToken()
            }
        }
        return localTokenSource.getToken()
    }

    private suspend fun refreshToken(): String? {
        val refreshToken = localTokenSource.getRefreshToken() ?: return null

        val tokenResponse = suspendCancellableCoroutine { cont ->
            val tokenRequest = TokenRequest.Builder(config, CLIENT_ID)
                .setGrantType(GrantTypeValues.REFRESH_TOKEN)
                .setRefreshToken(refreshToken)
                .build()

            authService.performTokenRequest(tokenRequest) { res, ex ->
                if (!cont.isActive) return@performTokenRequest

                if (ex != null) {
                    Timber.tag(TAG).e("Refresh failed: $ex")
                    cont.resume(null)
                    return@performTokenRequest
                }

                cont.resume(res)
            }
        }

        return if (tokenResponse?.accessToken != null) {
            val expiryTime = tokenResponse.accessTokenExpirationTime
            localTokenSource.setToken(tokenResponse.accessToken!!, expiryTime)
            if (tokenResponse.refreshToken != null) {
                localTokenSource.setRefreshToken(tokenResponse.refreshToken)
            }
            tokenResponse.accessToken
        } else {
            null
        }
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
        val tokenResponse = suspendCancellableCoroutine { cont ->
            val tokenRequest = authResponse.createTokenExchangeRequest()

            authService.performTokenRequest(tokenRequest) { res, ex ->
                if (!cont.isActive) return@performTokenRequest

                when {
                    ex != null -> {
                        Timber.tag(TAG).e("Msg: $ex")
                        cont.resumeWithException(ex)
                    }

                    res != null -> {
                        cont.resume(res)
                    }

                    else -> cont.resumeWithException(IllegalStateException("No response"))
                }
            }
        }

        tokenResponse.accessToken?.let {
            localTokenSource.setToken(it, tokenResponse.accessTokenExpirationTime)
        }
        tokenResponse.refreshToken?.let {
            localTokenSource.setRefreshToken(it)
        }
    }
}
