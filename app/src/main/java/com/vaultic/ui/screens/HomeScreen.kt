package com.vaultic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.vaultic.ui.VaulticViewModel
import com.vaultic.ui.components.CoinCard
import com.vaultic.ui.components.TokenSection
import com.vaultic.ui.components.WalletSelectorRow

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
            CoinCard(
                title = "Bitcoin",
                address = state.btcAddress,
                balance = state.btcBalance?.formatted ?: "--",
                onRefresh = { viewModel.refreshBalances() },
                onCopyAddress = { clipboard.setText(AnnotatedString(state.btcAddress)) }
            )
        }
        item {
            CoinCard(
                title = "Ethereum",
                address = state.ethAddress,
                balance = state.ethBalance?.formatted ?: "--",
                onRefresh = { viewModel.refreshBalances() },
                onCopyAddress = { clipboard.setText(AnnotatedString(state.ethAddress)) }
            )
        }
        item {
            TokenSection(
                tokens = viewModel.tokens(),
                balances = state.tokenBalances,
                onRefresh = { viewModel.refreshBalances() }
            )
        }
        if (state.error != null) {
            item {
                androidx.compose.material3.Text(
                    state.error ?: "",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
