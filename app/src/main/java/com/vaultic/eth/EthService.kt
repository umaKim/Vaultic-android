package com.vaultic.eth

import com.vaultic.core.BalanceDisplay
import com.vaultic.core.TransactionRecord
import com.vaultic.core.TransactionStatus
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.utils.Numeric
import java.math.BigInteger

class EthService(private val rpcClient: EthRpcClient) {
    suspend fun getBalance(rpcUrl: String, address: String): BalanceDisplay {
        val hex: String = rpcClient.call(rpcUrl, "eth_getBalance", listOf(address, "latest"))
        val wei = Numeric.decodeQuantity(hex)
        val formatted = formatEth(wei)
        return BalanceDisplay(formatted = formatted, raw = wei.toString())
    }

    suspend fun getGasPrice(rpcUrl: String): BigInteger {
        val hex: String = rpcClient.call(rpcUrl, "eth_gasPrice", listOf())
        return Numeric.decodeQuantity(hex)
    }

    suspend fun getTransactionCount(rpcUrl: String, address: String): BigInteger {
        val hex: String = rpcClient.call(rpcUrl, "eth_getTransactionCount", listOf(address, "pending"))
        return Numeric.decodeQuantity(hex)
    }

    suspend fun sendRawTransaction(rpcUrl: String, raw: String): String {
        return rpcClient.call(rpcUrl, "eth_sendRawTransaction", listOf(raw))
    }

    fun signTransaction(
        to: String,
        valueWei: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        nonce: BigInteger,
        chainId: Long,
        credentials: org.web3j.crypto.Credentials,
        data: String? = null
    ): String {
        val tx = RawTransaction.createTransaction(
            nonce,
            gasPrice,
            gasLimit,
            to,
            valueWei,
            data ?: "0x"
        )
        val signed = TransactionEncoder.signMessage(tx, chainId, credentials)
        return Numeric.toHexString(signed)
    }

    fun formatEth(wei: BigInteger, decimals: Int = 6): String {
        val divisor = BigInteger.TEN.pow(18)
        val integerPart = wei.divide(divisor)
        val fractionPart = wei.mod(divisor)
        val fractionString = fractionPart.toString().padStart(18, '0').take(decimals)
        return ("$integerPart.$fractionString").trimEnd('0').trimEnd('.')
    }

    fun toWei(amountEth: String): BigInteger? {
        return runCatching {
            val parts = amountEth.trim().split(".")
            val whole = parts[0].toBigInteger()
            val frac = parts.getOrNull(1)?.padEnd(18, '0')?.take(18) ?: "0"
            whole.multiply(BigInteger.TEN.pow(18)).add(frac.toBigInteger())
        }.getOrNull()
    }

    fun toGwei(amountGwei: String): BigInteger? {
        return runCatching {
            val parts = amountGwei.trim().split(".")
            val whole = parts[0].toBigInteger()
            val frac = parts.getOrNull(1)?.padEnd(9, '0')?.take(9) ?: "0"
            whole.multiply(BigInteger.TEN.pow(9)).add(frac.toBigInteger())
        }.getOrNull()
    }
}
