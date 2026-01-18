package com.vaultic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vaultic.ui.VaulticViewModel
import com.vaultic.ui.components.ActivityList

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
