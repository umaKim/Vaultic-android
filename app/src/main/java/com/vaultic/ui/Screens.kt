package com.vaultic.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vaultic.core.BalanceDisplay
import com.vaultic.core.TransactionRecord
import com.vaultic.core.WalletMetadata
import com.vaultic.token.ERC20Token

@Composable
fun OnboardingScreen(viewModel: VaulticViewModel) {
    var mnemonicInput by remember { mutableStateOf("") }
    var showMnemonic by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    if (showMnemonic != null) {
        AlertDialog(
            onDismissRequest = { showMnemonic = null },
            title = { Text("Write down your mnemonic") },
            text = { Text(showMnemonic ?: "") },
            confirmButton = {
                Button(onClick = { showMnemonic = null }) {
                    Text("I saved it")
                }
            }
        )
    }

    if (error != null) {
        AlertDialog(
            onDismissRequest = { error = null },
            title = { Text("Error") },
            text = { Text(error ?: "") },
            confirmButton = {
                Button(onClick = { error = null }) { Text("OK") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Vaultic", style = MaterialTheme.typography.headlineSmall)
        Text("Create or restore your wallet.")
        Button(onClick = {
            val mnemonic = viewModel.createWallet()
            showMnemonic = mnemonic
        }) {
            Text("Create Wallet")
        }
        OutlinedTextField(
            value = mnemonicInput,
            onValueChange = { mnemonicInput = it },
            label = { Text("Restore mnemonic") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        Button(onClick = {
            val ok = viewModel.restoreWallet(mnemonicInput)
            if (!ok) error = "Invalid mnemonic"
        }) {
            Text("Restore Wallet")
        }
    }
}

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

@Composable
fun ActivityScreen(viewModel: VaulticViewModel) {
    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Bitcoin", style = MaterialTheme.typography.titleMedium)
            ActivityList(state.btcTransactions)
        }
        item {
            Text("Ethereum", style = MaterialTheme.typography.titleMedium)
            ActivityList(state.ethTransactions)
        }
    }
}

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

@Composable
private fun WalletSelectorRow(
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
private fun AddressBlock(label: String, address: String, onCopy: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(address.ifEmpty { "--" })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onCopy) { Text("Copy") }
    }
}

@Composable
private fun CoinCard(title: String, address: String, balance: String, onRefresh: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = onRefresh) { Text("Refresh") }
            }
            Text("Address: ${address.ifEmpty { "--" }}")
            Text("Balance: $balance")
        }
    }
}

@Composable
private fun TokenSection(tokens: List<ERC20Token>, balances: Map<String, BalanceDisplay>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Tokens", style = MaterialTheme.typography.titleMedium)
        tokens.forEach { token ->
            val balance = balances[token.symbol]?.formatted ?: "--"
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(token.symbol, fontWeight = FontWeight.SemiBold)
                    Text(token.name)
                    Text("Balance: $balance")
                }
            }
        }
    }
}

@Composable
private fun ActivityList(records: List<TransactionRecord>) {
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
private fun TokenPicker(
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
