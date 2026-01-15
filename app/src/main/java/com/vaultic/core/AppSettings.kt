package com.vaultic.core

import android.content.Context

class AppSettings(context: Context) {
    private val prefs = context.getSharedPreferences("vaultic.settings", Context.MODE_PRIVATE)

    var rpcUrl: String
        get() = prefs.getString("rpcUrl", "") ?: ""
        set(value) { prefs.edit().putString("rpcUrl", value).apply() }

    var chainId: Int
        get() = prefs.getInt("chainId", 1)
        set(value) { prefs.edit().putInt("chainId", value).apply() }

    var btcApiBaseUrl: String
        get() = prefs.getString("btcApiBaseUrl", "https://mempool.space") ?: ""
        set(value) { prefs.edit().putString("btcApiBaseUrl", value).apply() }

    var ethExplorerBaseUrl: String
        get() = prefs.getString("ethExplorerBaseUrl", "https://eth.blockscout.com") ?: ""
        set(value) { prefs.edit().putString("ethExplorerBaseUrl", value).apply() }

    var appLockEnabled: Boolean
        get() = prefs.getBoolean("appLockEnabled", true)
        set(value) { prefs.edit().putBoolean("appLockEnabled", value).apply() }
}
