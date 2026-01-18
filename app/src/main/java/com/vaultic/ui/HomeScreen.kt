package com.vaultic.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(viewModel: VaulticViewModel) {
    val state by viewModel.state.collectAsState()
    val clipboard = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            WalletSelectorRow(
                wallets = state.wallets,
                activeWalletId = state.activeWalletId,
                onSelect = { viewModel.setActiveWallet(it) }
            )
        }
        item {
            AddressBlock(
                label = "ETH Address",
                address = state.ethAddress,
                onCopy = { clipboard.setText(AnnotatedString(state.ethAddress)) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            AddressBlock(
                label = "BTC Address",
                address = state.btcAddress,
                onCopy = { clipboard.setText(AnnotatedString(state.btcAddress)) }
            )
        }
        item {
            CoinCard(
                title = "Bitcoin",
                address = state.btcAddress,
                balance = state.btcBalance?.formatted ?: "--",
                onRefresh = { viewModel.refreshBalances() }
            )
        }
        item {
            CoinCard(
                title = "Ethereum",
                address = state.ethAddress,
                balance = state.ethBalance?.formatted ?: "--",
                onRefresh = { viewModel.refreshBalances() }
            )
        }
        item {
            TokenSection(
                tokens = viewModel.tokens(),
                balances = state.tokenBalances
            )
        }
    }
}
