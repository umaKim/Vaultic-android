package com.vaultic.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vaultic.btc.BtcApiClient
import com.vaultic.btc.BtcService
import com.vaultic.btc.BtcWalletService
import com.vaultic.core.AppSettings
import com.vaultic.core.BalanceDisplay
import com.vaultic.core.TransactionRecord
import com.vaultic.core.WalletManager
import com.vaultic.core.WalletMetadata
import com.vaultic.core.secure.SecureSeedVault
import com.vaultic.eth.EthHistoryClient
import com.vaultic.eth.EthRpcClient
import com.vaultic.eth.EthService
import com.vaultic.eth.EthWalletService
import com.vaultic.token.ERC20Token
import com.vaultic.token.TokenService
import com.vaultic.token.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigInteger

private const val DEFAULT_WALLET_NAME_PREFIX = "Wallet"

data class WalletUiState(
    val wallets: List<WalletMetadata> = emptyList(),
    val activeWalletId: String? = null,
    val activeWalletName: String = "",
    val ethAddress: String = "",
    val btcAddress: String = "",
    val ethBalance: BalanceDisplay? = null,
    val btcBalance: BalanceDisplay? = null,
    val tokenBalances: Map<String, BalanceDisplay> = emptyMap(),
    val ethTransactions: List<TransactionRecord> = emptyList(),
    val btcTransactions: List<TransactionRecord> = emptyList(),
    val rpcUrl: String = "",
    val chainId: Int = 1,
    val btcApiBaseUrl: String = "",
    val ethExplorerBaseUrl: String = "",
    val appLockEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)

class VaulticViewModel(application: Application) : AndroidViewModel(application) {
    private val walletManager = WalletManager(application)
    private val seedVault = SecureSeedVault(application)
    private val settings = AppSettings(application)

    private val ethWalletService = EthWalletService()
    private val btcWalletService = BtcWalletService()

    private val ethRpc = EthRpcClient()
    private val ethService = EthService(ethRpc)
    private val ethHistoryClient = EthHistoryClient()

    private val btcService = BtcService(BtcApiClient())

    private val tokenStore = TokenStore(application)
    private val tokenService = TokenService(ethRpc)

    private val _state = MutableStateFlow(loadInitialState())
    val state: StateFlow<WalletUiState> = _state

    init {
        refreshActiveWalletContext()
    }

    fun createWallet(): String {
        val mnemonic = ethWalletService.generateMnemonic()
        val name = nextWalletName()
        val wallet = walletManager.addWallet(name)
        seedVault.storeMnemonic(wallet.id, mnemonic)
        updateWalletList()
        setActiveWallet(wallet.id)
        return mnemonic
    }

    fun restoreWallet(mnemonic: String): Boolean {
        if (!ethWalletService.validateMnemonic(mnemonic)) return false
        val normalized = ethWalletService.normalizeMnemonic(mnemonic)
        val name = nextWalletName()
        val wallet = walletManager.addWallet(name)
        seedVault.storeMnemonic(wallet.id, normalized)
        updateWalletList()
        setActiveWallet(wallet.id)
        return true
    }

    fun renameWallet(id: String, name: String) {
        walletManager.renameWallet(id, name)
        updateWalletList()
    }

    fun removeWallet(id: String) {
        walletManager.removeWallet(id)
        seedVault.deleteMnemonic(id)
        updateWalletList()
        refreshActiveWalletContext()
    }

    fun setActiveWallet(id: String) {
        walletManager.setActiveWallet(id)
        updateWalletList()
        refreshActiveWalletContext()
    }

    fun updateRpcUrl(value: String) {
        settings.rpcUrl = value
        _state.value = _state.value.copy(rpcUrl = value)
    }

    fun updateChainId(value: Int) {
        settings.chainId = value
        _state.value = _state.value.copy(chainId = value)
    }

    fun updateBtcApiBaseUrl(value: String) {
        settings.btcApiBaseUrl = value
        _state.value = _state.value.copy(btcApiBaseUrl = value)
    }

    fun updateEthExplorerBaseUrl(value: String) {
        settings.ethExplorerBaseUrl = value
        _state.value = _state.value.copy(ethExplorerBaseUrl = value)
    }

    fun updateAppLockEnabled(value: Boolean) {
        settings.appLockEnabled = value
        _state.value = _state.value.copy(appLockEnabled = value)
    }

    fun refreshBalances() {
        val walletId = walletManager.activeWalletId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val mnemonic = requireMnemonic(walletId)
                val ethAddress = ethWalletService.deriveAddress(mnemonic)
                val btcAddress = btcWalletService.deriveAddress(mnemonic)
                val ethBalance = ethService.getBalance(settings.rpcUrl, ethAddress)
                val btcBalance = btcService.getBalance(settings.btcApiBaseUrl, btcAddress)
                val tokens = tokenStore.tokens(settings.chainId)
                val tokenBalances = tokens.associate { token ->
                    token.symbol to tokenService.balanceOf(settings.rpcUrl, token, ethAddress)
                }
                updateState {
                    it.copy(
                        ethAddress = ethAddress,
                        btcAddress = btcAddress,
                        ethBalance = ethBalance,
                        btcBalance = btcBalance,
                        tokenBalances = tokenBalances,
                        isLoading = false,
                        error = null
                    )
                }
            }.onFailure { error ->
                updateState { it.copy(isLoading = false, error = error.message ?: "Failed to refresh balances") }
            }
        }
        updateState { it.copy(isLoading = true, error = null) }
    }

    fun refreshHistory() {
        val walletId = walletManager.activeWalletId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val mnemonic = requireMnemonic(walletId)
                val ethAddress = ethWalletService.deriveAddress(mnemonic)
                val btcAddress = btcWalletService.deriveAddress(mnemonic)
                val ethTxs = ethHistoryClient.fetchTransactions(settings.ethExplorerBaseUrl, ethAddress)
                val btcTxs = btcService.fetchHistory(settings.btcApiBaseUrl, btcAddress)
                updateState { it.copy(ethTransactions = ethTxs, btcTransactions = btcTxs, error = null) }
            }.onFailure { error ->
                updateState { it.copy(error = error.message ?: "Failed to fetch activity") }
            }
        }
    }

    fun sendEth(to: String, amountEth: String, gasPriceGwei: String, gasLimit: String) {
        val walletId = walletManager.activeWalletId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val mnemonic = requireMnemonic(walletId)
                val creds = ethWalletService.deriveCredentials(mnemonic)
                val valueWei = ethService.toWei(amountEth) ?: throw IllegalArgumentException("Invalid amount")
                val gasPrice = ethService.toGwei(gasPriceGwei) ?: throw IllegalArgumentException("Invalid gas price")
                val gasLimitValue = gasLimit.toBigIntegerOrNull() ?: throw IllegalArgumentException("Invalid gas limit")
                val nonce = ethService.getTransactionCount(settings.rpcUrl, creds.address)
                val raw = ethService.signTransaction(
                    to = to,
                    valueWei = valueWei,
                    gasPrice = gasPrice,
                    gasLimit = gasLimitValue,
                    nonce = nonce,
                    chainId = settings.chainId.toLong(),
                    credentials = creds
                )
                val hash = ethService.sendRawTransaction(settings.rpcUrl, raw)
                refreshHistory()
                updateState { it.copy(error = null) }
                hash
            }.onFailure { error ->
                updateState { it.copy(error = error.message ?: "Failed to send ETH") }
            }
        }
    }

    fun sendToken(token: ERC20Token, to: String, amount: String, gasPriceGwei: String, gasLimit: String) {
        val walletId = walletManager.activeWalletId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val mnemonic = requireMnemonic(walletId)
                val creds = ethWalletService.deriveCredentials(mnemonic)
                val amountValue = tokenService.toTokenAmount(amount, token.decimals)
                    ?: throw IllegalArgumentException("Invalid amount")
                val data = tokenService.buildTransferData(to, amountValue)
                val gasPrice = ethService.toGwei(gasPriceGwei) ?: throw IllegalArgumentException("Invalid gas price")
                val gasLimitValue = gasLimit.toBigIntegerOrNull() ?: throw IllegalArgumentException("Invalid gas limit")
                val nonce = ethService.getTransactionCount(settings.rpcUrl, creds.address)
                val raw = ethService.signTransaction(
                    to = token.contract,
                    valueWei = BigInteger.ZERO,
                    gasPrice = gasPrice,
                    gasLimit = gasLimitValue,
                    nonce = nonce,
                    chainId = settings.chainId.toLong(),
                    credentials = creds,
                    data = data
                )
                val hash = ethService.sendRawTransaction(settings.rpcUrl, raw)
                refreshHistory()
                updateState { it.copy(error = null) }
                hash
            }.onFailure { error ->
                updateState { it.copy(error = error.message ?: "Failed to send token") }
            }
        }
    }

    fun sendBtc(to: String, amountBtc: String, feeRate: String) {
        val walletId = walletManager.activeWalletId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val mnemonic = requireMnemonic(walletId)
                val fromAddress = btcWalletService.deriveAddress(mnemonic)
                val key = btcWalletService.deriveKey(mnemonic)
                val amountSats = btcService.toSats(amountBtc) ?: throw IllegalArgumentException("Invalid amount")
                val rate = feeRate.toLongOrNull() ?: throw IllegalArgumentException("Invalid fee rate")
                val utxos = btcService.getUtxos(settings.btcApiBaseUrl, fromAddress)
                val result = btcService.buildAndSign(
                    utxos = utxos,
                    fromAddress = fromAddress,
                    toAddress = to,
                    amountSats = amountSats,
                    feeRate = rate,
                    privateKeyBytes = key.privKeyBytes
                )
                val txid = btcService.sendRawTransaction(settings.btcApiBaseUrl, result.rawTx)
                refreshHistory()
                updateState { it.copy(error = null) }
                txid
            }.onFailure { error ->
                updateState { it.copy(error = error.message ?: "Failed to send BTC") }
            }
        }
    }

    fun revealMnemonic(walletId: String): String? = seedVault.loadMnemonic(walletId)

    private fun refreshActiveWalletContext() {
        val walletId = walletManager.activeWalletId
        if (walletId == null) {
            updateState { it.copy(ethAddress = "", btcAddress = "", ethBalance = null, btcBalance = null) }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val mnemonic = requireMnemonic(walletId)
                val ethAddress = ethWalletService.deriveAddress(mnemonic)
                val btcAddress = btcWalletService.deriveAddress(mnemonic)
                updateState { it.copy(ethAddress = ethAddress, btcAddress = btcAddress, error = null) }
                refreshBalances()
                refreshHistory()
            }.onFailure { error ->
                updateState { it.copy(error = error.message ?: "Failed to load wallet") }
            }
        }
    }

    private fun updateWalletList() {
        _state.value = _state.value.copy(
            wallets = walletManager.wallets,
            activeWalletId = walletManager.activeWalletId,
            activeWalletName = walletManager.walletName(walletManager.activeWalletId) ?: ""
        )
    }

    private fun updateState(update: (WalletUiState) -> WalletUiState) {
        _state.value = update(_state.value)
    }

    private fun requireMnemonic(walletId: String): String {
        return seedVault.loadMnemonic(walletId) ?: throw IllegalStateException("Missing mnemonic")
    }

    private fun loadInitialState(): WalletUiState {
        val active = walletManager.activeWalletId
        return WalletUiState(
            wallets = walletManager.wallets,
            activeWalletId = active,
            activeWalletName = walletManager.walletName(active) ?: "",
            rpcUrl = settings.rpcUrl,
            chainId = settings.chainId,
            btcApiBaseUrl = settings.btcApiBaseUrl,
            ethExplorerBaseUrl = settings.ethExplorerBaseUrl,
            appLockEnabled = settings.appLockEnabled
        )
    }

    fun tokens(): List<com.vaultic.token.ERC20Token> = tokenStore.tokens(settings.chainId)

    private fun nextWalletName(): String {
        val index = walletManager.wallets.size + 1
        return "$DEFAULT_WALLET_NAME_PREFIX $index"
    }
}
