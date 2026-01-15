package com.vaultic.core.secure

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executors

class BiometricGate(private val activity: FragmentActivity) {
    fun authenticate(reason: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val manager = BiometricManager.from(activity)
        val canAuth = manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            onError("Biometrics unavailable")
            return
        }

        val executor = Executors.newSingleThreadExecutor()
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                activity.runOnUiThread { onSuccess() }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                activity.runOnUiThread { onError(errString.toString()) }
            }

            override fun onAuthenticationFailed() {
                activity.runOnUiThread { onError("Authentication failed") }
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Vaultic")
            .setSubtitle(reason)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        prompt.authenticate(promptInfo)
    }
}
