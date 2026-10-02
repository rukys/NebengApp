package com.disinidev.nebeng.core.auth

import android.content.Context
import android.content.res.Resources
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.disinidev.nebeng.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import timber.log.Timber

object GoogleSignInHelper {
    const val DEFAULT_WEB_CLIENT_ID = "192893427499-m1qk75hnnkoplvpa5ve0cgmg6l3b7oet.apps.googleusercontent.com"

    suspend fun getGoogleIdToken(context: Context): String? {
        val credentialManager = CredentialManager.create(context)
        val serverClientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (_: Resources.NotFoundException) {
            DEFAULT_WEB_CLIENT_ID
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                GoogleIdTokenCredential.createFrom(credential.data).idToken
            } else {
                null
            }
        } catch (e: NoCredentialException) {
            // No saved Google accounts on device — caller should show manual login
            Timber.d("GoogleSignIn: no credential available")
            null
        } catch (e: GetCredentialCancellationException) {
            // User dismissed the credential picker
            Timber.d("GoogleSignIn: user cancelled credential picker")
            null
        } catch (e: GetCredentialException) {
            Timber.e(e, "GoogleSignIn: getCredential failed")
            null
        }
    }
}

