package com.vaultic.token

import kotlinx.serialization.Serializable

@Serializable
data class ERC20Token(
    val symbol: String,
    val name: String,
    val contract: String,
    val decimals: Int,
    val chainId: Int,
    val verified: Boolean
)
