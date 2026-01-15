package com.vaultic.btc

import org.bitcoinj.core.LegacyAddress
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.wallet.DeterministicKeyChain
import org.bitcoinj.wallet.DeterministicSeed
import org.bitcoinj.params.MainNetParams
import org.web3j.crypto.MnemonicUtils

class BtcWalletService {
    private val params = MainNetParams.get()

    fun generateMnemonic(): String {
        val entropy = ByteArray(16)
        java.security.SecureRandom().nextBytes(entropy)
        return MnemonicUtils.generateMnemonic(entropy)
    }

    fun normalizeMnemonic(mnemonic: String): String = mnemonic
        .lowercase()
        .split(" ", "\n", "\t")
        .filter { it.isNotBlank() }
        .joinToString(" ")

    fun validateMnemonic(mnemonic: String): Boolean = try {
        MnemonicUtils.validateMnemonic(normalizeMnemonic(mnemonic))
    } catch (_: Exception) {
        false
    }

    fun deriveKey(mnemonic: String): DeterministicKey {
        val words = normalizeMnemonic(mnemonic).split(" ")
        val seed = DeterministicSeed(words, null, "", 0L)
        val chain = DeterministicKeyChain.builder().seed(seed).build()
        val path = listOf(
            ChildNumber(44, true),
            ChildNumber(0, true),
            ChildNumber(0, true),
            ChildNumber(0, false),
            ChildNumber(0, false)
        )
        return chain.getKeyByPath(path, true)
    }

    fun deriveAddress(mnemonic: String): String {
        val key = deriveKey(mnemonic)
        return LegacyAddress.fromKey(params, key).toString()
    }
}
