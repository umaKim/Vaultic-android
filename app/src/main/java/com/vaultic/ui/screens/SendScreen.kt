package com.vaultic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vaultic.ui.VaulticViewModel
import com.vaultic.ui.components.TokenPicker
import com.vaultic.ui.components.WalletSelectorRow

@Composable
fun SendScreen(viewModel: VaulticViewModel) {
    val state by viewModel.state.collectAsState()
    val tokens = viewModel.tokens()
    var tabIndex by remember { mutableStateOf(0) }

    var toAddress by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var gasPrice by remember { mutableStateOf("15") }
    var gasLimit by remember { mutableStateOf("21000") }
    var feeRate by remember { mutableStateOf("10") }
    var selectedToken by remember { mutableStateOf(tokens.firstOrNull()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        WalletSelectorRow(
            wallets = state.wallets,
            activeWalletId = state.activeWalletId,
            onSelect = { viewModel.setActiveWallet(it) }
        )
        TabRow(selectedTabIndex = tabIndex) {
            Tab(text = { Text("BTC") }, selected = tabIndex == 0, onClick = { tabIndex = 0 })
            Tab(text = { Text("ETH") }, selected = tabIndex == 1, onClick = { tabIndex = 1 })
            Tab(text = { Text("Token") }, selected = tabIndex == 2, onClick = { tabIndex = 2 })
        }
        OutlinedTextField(
            value = toAddress,
            onValueChange = { toAddress = it },
            label = { Text("To Address") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )

        if (tabIndex == 0) {
            OutlinedTextField(
                value = feeRate,
                onValueChange = { feeRate = it },
                label = { Text("Fee rate (sat/vB)") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { viewModel.sendBtc(toAddress, amount, feeRate) }) {
                Text("Send BTC")
            }
        } else if (tabIndex == 1) {
            OutlinedTextField(
                value = gasPrice,
                onValueChange = { gasPrice = it },
                label = { Text("Gas price (Gwei)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = gasLimit,
                onValueChange = { gasLimit = it },
                label = { Text("Gas limit") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { viewModel.sendEth(toAddress, amount, gasPrice, gasLimit) }) {
                Text("Send ETH")
            }
        } else {
            TokenPicker(tokens = tokens, selectedToken = selectedToken, onSelect = { selectedToken = it })
            OutlinedTextField(
                value = gasPrice,
                onValueChange = { gasPrice = it },
                label = { Text("Gas price (Gwei)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = gasLimit,
                onValueChange = { gasLimit = it },
                label = { Text("Gas limit") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
                selectedToken?.let { token ->
                    viewModel.sendToken(token, toAddress, amount, gasPrice, gasLimit)
                }
            }) {
                Text("Send Token")
            }
        }
        if (state.error != null) {
            Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
        }
    }
}
