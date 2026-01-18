package com.vaultic.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vaultic.core.WalletMetadata

@Composable
fun SettingsScreen(
    viewModel: VaulticViewModel,
    onRequireBiometric: (String, () -> Unit, (String) -> Unit) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var renameTarget by remember { mutableStateOf<WalletMetadata?>(null) }
    var newName by remember { mutableStateOf("") }
    var revealMnemonics by remember { mutableStateOf(false) }
    var revealError by remember { mutableStateOf<String?>(null) }

    if (renameTarget != null) {
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename wallet") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Wallet name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    val target = renameTarget
                    if (target != null && newName.isNotBlank()) {
                        viewModel.renameWallet(target.id, newName)
                    }
                    renameTarget = null
                }) { Text("Save") }
            }
        )
    }

    if (revealMnemonics) {
        AlertDialog(
            onDismissRequest = { revealMnemonics = false },
            title = { Text("Saved mnemonics") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.wallets.forEach { wallet ->
                        val mnemonic = viewModel.revealMnemonic(wallet.id) ?: "(missing)"
                        Text("${wallet.name}: $mnemonic")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { revealMnemonics = false }) { Text("Close") }
            }
        )
    }

    if (revealError != null) {
        AlertDialog(
            onDismissRequest = { revealError = null },
            title = { Text("Error") },
            text = { Text(revealError ?: "") },
            confirmButton = { Button(onClick = { revealError = null }) { Text("OK") } }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Network", style = MaterialTheme.typography.titleMedium) }
        item {
            OutlinedTextField(
                value = state.rpcUrl,
                onValueChange = { viewModel.updateRpcUrl(it) },
                label = { Text("ETH RPC URL") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = state.chainId.toString(),
                onValueChange = { viewModel.updateChainId(it.toIntOrNull() ?: 1) },
                label = { Text("Chain ID") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = state.btcApiBaseUrl,
                onValueChange = { viewModel.updateBtcApiBaseUrl(it) },
                label = { Text("BTC API Base URL") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = state.ethExplorerBaseUrl,
                onValueChange = { viewModel.updateEthExplorerBaseUrl(it) },
                label = { Text("ETH Explorer Base URL") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("App Lock", modifier = Modifier.weight(1f))
                Switch(
                    checked = state.appLockEnabled,
                    onCheckedChange = { viewModel.updateAppLockEnabled(it) }
                )
            }
        }
        item {
            Button(onClick = {
                onRequireBiometric(
                    "Reveal mnemonics",
                    { revealMnemonics = true },
                    { error -> revealError = error }
                )
            }) {
                Text("Reveal mnemonics")
            }
        }
        item { Divider() }
        item { Text("Wallets", style = MaterialTheme.typography.titleMedium) }
        items(state.wallets) { wallet ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(wallet.name, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            renameTarget = wallet
                            newName = wallet.name
                        }) {
                            Text("Rename")
                        }
                        OutlinedButton(onClick = { viewModel.removeWallet(wallet.id) }) {
                            Text("Remove")
                        }
                    }
                }
            }
        }
    }
}
