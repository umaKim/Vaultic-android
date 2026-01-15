package com.vaultic.token

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TokenStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("vaultic.tokens", Context.MODE_PRIVATE)

    fun tokens(chainId: Int): List<ERC20Token> {
        val base = loadBundledTokens().filter { it.chainId == chainId }
        val custom = loadCustomTokens().filter { it.chainId == chainId }
        return (base + custom)
            .groupBy { it.contract.lowercase() }
            .map { it.value.last() }
            .sortedBy { it.symbol }
    }

    fun addCustomToken(token: ERC20Token) {
        val all = loadCustomTokens().toMutableList()
        all.removeAll { it.chainId == token.chainId && it.contract.equals(token.contract, true) }
        all.add(token)
        prefs.edit().putString("custom", Json.encodeToString(all)).apply()
    }

    private fun loadBundledTokens(): List<ERC20Token> {
        val json = context.assets.open("tokens.json").bufferedReader().use { it.readText() }
        return runCatching { Json.decodeFromString<List<ERC20Token>>(json) }.getOrDefault(emptyList())
    }

    private fun loadCustomTokens(): List<ERC20Token> {
        val raw = prefs.getString("custom", null) ?: return emptyList()
        return runCatching { Json.decodeFromString<List<ERC20Token>>(raw) }.getOrDefault(emptyList())
    }
}
