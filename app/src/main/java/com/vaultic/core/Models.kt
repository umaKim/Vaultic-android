package com.vaultic.core

enum class TransactionStatus {
    Pending,
    Confirmed,
    Failed
}

data class TransactionRecord(
    val hash: String,
    val status: TransactionStatus = TransactionStatus.Pending,
    val confirmations: Int = 0
)

data class BalanceDisplay(
    val formatted: String,
    val raw: String
)
