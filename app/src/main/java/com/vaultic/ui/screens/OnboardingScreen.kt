package com.vaultic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vaultic.ui.VaulticViewModel

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
