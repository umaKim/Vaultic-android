package com.vaultic.btc

import com.vaultic.core.BalanceDisplay
import com.vaultic.core.TransactionRecord
import com.vaultic.core.TransactionStatus
import org.bitcoinj.params.MainNetParams

class BtcService(private val api: BtcApiClient) {
    private val params = MainNetParams.get()

    suspend fun getBalance(baseUrl: String, address: String): BalanceDisplay {
        val trimmed = baseUrl.trimEnd('/')
        if (trimmed.isEmpty()) return BalanceDisplay("0", "0")
        val response: BtcAddressResponse = api.get("$trimmed/api/address/$address")
        val funded = response.chain_stats.funded_txo_sum + response.mempool_stats.funded_txo_sum
        val spent = response.chain_stats.spent_txo_sum + response.mempool_stats.spent_txo_sum
        val sats = (funded - spent).coerceAtLeast(0)
        return BalanceDisplay(formatBtc(sats), sats.toString())
    }

    suspend fun getUtxos(baseUrl: String, address: String): List<BtcUtxo> {
        val trimmed = baseUrl.trimEnd('/')
        if (trimmed.isEmpty()) return emptyList()
        return api.get("$trimmed/api/address/$address/utxo")
    }

    suspend fun sendRawTransaction(baseUrl: String, rawHex: String): String {
        val trimmed = baseUrl.trimEnd('/')
        if (trimmed.isEmpty()) throw IllegalArgumentException("Missing BTC API URL")
        return api.postText("$trimmed/api/tx", rawHex)
    }

    suspend fun fetchHistory(baseUrl: String, address: String): List<TransactionRecord> {
        val trimmed = baseUrl.trimEnd('/')
        if (trimmed.isEmpty()) return emptyList()
        val response: List<BtcTxSummary> = api.get("$trimmed/api/address/$address/txs")
        return response.map { item ->
            val status = if (item.status.confirmed) TransactionStatus.Confirmed else TransactionStatus.Pending
            val confirmations = if (item.status.confirmed && item.status.block_height != null) 1 else 0
            TransactionRecord(hash = item.txid, status = status, confirmations = confirmations)
        }
    }

    fun buildAndSign(
        utxos: List<BtcUtxo>,
        fromAddress: String,
        toAddress: String,
        amountSats: Long,
        feeRate: Long,
        privateKeyBytes: ByteArray
    ): BtcTxBuilder.BuildResult {
        val builder = BtcTxBuilder(params)
        return builder.buildAndSign(utxos, fromAddress, toAddress, amountSats, feeRate, privateKeyBytes)
    }

    fun formatBtc(sats: Long, decimals: Int = 8): String {
        val divisor = 100_000_000L
        val whole = sats / divisor
        val fraction = (sats % divisor).toString().padStart(8, '0').take(decimals)
        return ("$whole.$fraction").trimEnd('0').trimEnd('.')
    }

    fun toSats(amountBtc: String): Long? {
        return runCatching {
            val parts = amountBtc.trim().split(".")
            val whole = parts[0].toLong()
            val frac = parts.getOrNull(1)?.padEnd(8, '0')?.take(8) ?: "0"
            whole * 100_000_000L + frac.toLong()
        }.getOrNull()
    }
}
