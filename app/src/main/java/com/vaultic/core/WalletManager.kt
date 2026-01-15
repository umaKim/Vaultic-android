package com.vaultic.core

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class WalletMetadata(
    val id: String,
    val name: String,
    val createdAt: Long
)

class WalletManager(context: Context) {
    private val prefs = context.getSharedPreferences("vaultic.wallets", Context.MODE_PRIVATE)

    var wallets: List<WalletMetadata> = loadWallets()
        private set

    var activeWalletId: String? = prefs.getString("activeWalletId", null)
        private set

    fun addWallet(name: String, id: String = java.util.UUID.randomUUID().toString()): WalletMetadata {
        val wallet = WalletMetadata(id, name, System.currentTimeMillis())
        wallets = wallets + wallet
        if (activeWalletId == null) {
            activeWalletId = wallet.id
        }
        persist()
        return wallet
    }

    fun setActiveWallet(id: String) {
        if (wallets.any { it.id == id }) {
            activeWalletId = id
            persist()
        }
    }

    fun renameWallet(id: String, name: String) {
        wallets = wallets.map { if (it.id == id) it.copy(name = name) else it }
        persist()
    }

    fun removeWallet(id: String) {
        wallets = wallets.filterNot { it.id == id }
        if (activeWalletId == id) {
            activeWalletId = wallets.firstOrNull()?.id
        }
        persist()
    }

    fun walletName(id: String?): String? = wallets.firstOrNull { it.id == id }?.name

    private fun loadWallets(): List<WalletMetadata> {
        val raw = prefs.getString("wallets", null) ?: return emptyList()
        return runCatching { Json.decodeFromString<List<WalletMetadata>>(raw) }.getOrDefault(emptyList())
    }

    private fun persist() {
        prefs.edit().putString("wallets", Json.encodeToString(wallets))
            .putString("activeWalletId", activeWalletId)
            .apply()
    }
}
