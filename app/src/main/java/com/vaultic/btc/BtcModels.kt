package com.vaultic.btc

import kotlinx.serialization.Serializable

@Serializable
data class BtcAddressStats(
    val funded_txo_sum: Long = 0,
    val spent_txo_sum: Long = 0
)

@Serializable
data class BtcAddressResponse(
    val address: String,
    val chain_stats: BtcAddressStats,
    val mempool_stats: BtcAddressStats
)

@Serializable
data class BtcUtxoStatus(
    val confirmed: Boolean = false,
    val block_height: Long? = null,
    val block_time: Long? = null
)

@Serializable
data class BtcUtxo(
    val txid: String,
    val vout: Int,
    val value: Long,
    val status: BtcUtxoStatus
)

@Serializable
data class BtcTxStatus(
    val confirmed: Boolean = false,
    val block_height: Long? = null
)

@Serializable
data class BtcTxSummary(
    val txid: String,
    val status: BtcTxStatus
)
