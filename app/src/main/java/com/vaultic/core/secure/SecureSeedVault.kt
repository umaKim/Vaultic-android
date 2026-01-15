package com.vaultic.core.secure

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureSeedVault(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "vaultic.mnemonics",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun storeMnemonic(walletId: String, mnemonic: String) {
        prefs.edit().putString(key(walletId), mnemonic).apply()
    }

    fun loadMnemonic(walletId: String): String? = prefs.getString(key(walletId), null)

    fun hasMnemonic(walletId: String): Boolean = prefs.contains(key(walletId))

    fun deleteMnemonic(walletId: String) {
        prefs.edit().remove(key(walletId)).apply()
    }

    private fun key(walletId: String) = "mnemonic.$walletId"
}
