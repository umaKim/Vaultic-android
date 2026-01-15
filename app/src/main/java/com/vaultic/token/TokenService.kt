package com.vaultic.token

import com.vaultic.core.BalanceDisplay
import com.vaultic.eth.EthRpcClient
import org.web3j.utils.Numeric
import java.math.BigInteger

class TokenService(private val rpcClient: EthRpcClient) {
    suspend fun balanceOf(rpcUrl: String, token: ERC20Token, owner: String): BalanceDisplay {
        val data = buildBalanceOfData(owner)
        val call = mapOf(
            "to" to token.contract,
            "data" to data
        )
        val result: String = rpcClient.call(rpcUrl, "eth_call", listOf(call, "latest"))
        val value = Numeric.decodeQuantity(result)
        return BalanceDisplay(formatToken(value, token.decimals), value.toString())
    }

    fun buildTransferData(to: String, amount: BigInteger): String {
        val methodId = "a9059cbb"
        val toPadded = cleanHex(to).padStart(64, '0')
        val amountPadded = amount.toString(16).padStart(64, '0')
        return "0x$methodId$toPadded$amountPadded"
    }

    fun toTokenAmount(amount: String, decimals: Int): BigInteger? {
        return runCatching {
            val parts = amount.trim().split(".")
            val whole = parts[0].toBigInteger()
            val frac = parts.getOrNull(1)?.padEnd(decimals, '0')?.take(decimals) ?: "0"
            whole.multiply(BigInteger.TEN.pow(decimals)).add(frac.toBigInteger())
        }.getOrNull()
    }

    private fun buildBalanceOfData(address: String): String {
        val methodId = "70a08231"
        val addr = cleanHex(address).padStart(64, '0')
        return "0x$methodId$addr"
    }

    private fun cleanHex(input: String): String = input.removePrefix("0x").lowercase()

    private fun formatToken(amount: BigInteger, decimals: Int, displayDecimals: Int = 6): String {
        val divisor = BigInteger.TEN.pow(decimals)
        val whole = amount.divide(divisor)
        val fraction = amount.mod(divisor).toString().padStart(decimals, '0').take(displayDecimals)
        return ("$whole.$fraction").trimEnd('0').trimEnd('.')
    }
}
