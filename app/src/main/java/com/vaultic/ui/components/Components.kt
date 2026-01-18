package com.vaultic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vaultic.core.BalanceDisplay
import com.vaultic.core.TransactionRecord
import com.vaultic.core.WalletMetadata
import com.vaultic.token.ERC20Token

@Composable
fun WalletSelectorRow(
    wallets: List<WalletMetadata>,
    activeWalletId: String?,
    onSelect: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(wallets) { wallet ->
            val selected = wallet.id == activeWalletId
            val background = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
            val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            Box(
                modifier = Modifier
                    .background(background, RoundedCornerShape(16.dp))
                    .clickable { onSelect(wallet.id) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(wallet.name, color = contentColor)
            }
        }
    }
}

@Composable
fun CoinCard(
    title: String,
    address: String,
    balance: String,
    onRefresh: () -> Unit,
    onCopyAddress: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = onRefresh) { Text("Refresh") }
            }
            Text("Address", style = MaterialTheme.typography.labelMedium)
            Text(
                text = address.ifEmpty { "--" },
                modifier = Modifier.clickable { onCopyAddress() }
            )
            Text("Balance: $balance")
        }
    }
}

@Composable
fun TokenSection(
    tokens: List<ERC20Token>,
    balances: Map<String, BalanceDisplay>,
    onRefresh: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row {
                Text("Tokens", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = onRefresh) { Text("Refresh") }
            }
            tokens.forEach { token ->
                val balance = balances[token.symbol]?.formatted ?: "--"
                val symbol = token.symbol
                val highlight = symbol.equals("USDC", ignoreCase = true) ||
                    symbol.equals("USDT", ignoreCase = true)
                val content: @Composable () -> Unit = {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(token.symbol, fontWeight = FontWeight.SemiBold)
                        Text(token.name)
                        Text("Balance: $balance")
                    }
                }
                if (highlight) {
                    val highlightColor = if (symbol.equals("USDC", ignoreCase = true)) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = highlightColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        content()
                    }
                } else {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityList(records: List<TransactionRecord>) {
    if (records.isEmpty()) {
        Text("No activity")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        records.forEach { record ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(record.hash, maxLines = 1)
                    Text("Status: ${record.status}")
                    Text("Confirmations: ${record.confirmations}")
                }
            }
        }
    }
}

@Composable
fun TokenPicker(
    tokens: List<ERC20Token>,
    selectedToken: ERC20Token?,
    onSelect: (ERC20Token) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val label = selectedToken?.symbol ?: "Select token"

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(label)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            tokens.forEach { token ->
                DropdownMenuItem(
                    text = { Text(token.symbol) },
                    onClick = {
                        onSelect(token)
                        expanded = false
                    }
                )
            }
        }
    }
}
