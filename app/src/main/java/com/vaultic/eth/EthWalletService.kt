package com.vaultic.eth

import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Credentials
import org.web3j.crypto.Keys
import org.web3j.crypto.MnemonicUtils

class EthWalletService {
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

    fun deriveCredentials(mnemonic: String): Credentials {
        val seed = MnemonicUtils.generateSeed(normalizeMnemonic(mnemonic), "")
        val master = Bip32ECKeyPair.generateKeyPair(seed)
        val path = intArrayOf(
            44 or Bip32ECKeyPair.HARDENED_BIT,
            60 or Bip32ECKeyPair.HARDENED_BIT,
            0 or Bip32ECKeyPair.HARDENED_BIT,
            0,
            0
        )
        val derived = Bip32ECKeyPair.deriveKeyPair(master, path)
        return Credentials.create(derived)
    }

    fun deriveAddress(mnemonic: String): String {
        val creds = deriveCredentials(mnemonic)
        return Keys.toChecksumAddress(creds.address)
    }
}
