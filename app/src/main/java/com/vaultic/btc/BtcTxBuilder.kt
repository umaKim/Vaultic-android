package com.vaultic.btc

import org.bitcoinj.core.ECKey
import org.bitcoinj.core.LegacyAddress
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionInput
import org.bitcoinj.core.TransactionOutPoint
import org.bitcoinj.core.Utils
import org.bitcoinj.script.ScriptBuilder

class BtcTxBuilder(private val params: NetworkParameters) {
    data class BuildResult(
        val rawTx: String,
        val feePaid: Long,
        val changeValue: Long
    )

    fun buildAndSign(
        utxos: List<BtcUtxo>,
        fromAddress: String,
        toAddress: String,
        amountSats: Long,
        feeRate: Long,
        privateKeyBytes: ByteArray
    ): BuildResult {
        val selected = selectUtxos(utxos, amountSats, feeRate)
        val totalIn = selected.sumOf { it.value }
        val fee = estimateFee(selected.size, 2, feeRate)
        val change = totalIn - amountSats - fee
        if (change < 0) throw IllegalArgumentException("Insufficient funds")

        val tx = Transaction(params)
        val to = LegacyAddress.fromBase58(params, toAddress)
        tx.addOutput(org.bitcoinj.core.Coin.valueOf(amountSats), to)
        if (change > 0) {
            val changeAddr = LegacyAddress.fromBase58(params, fromAddress)
            tx.addOutput(org.bitcoinj.core.Coin.valueOf(change), changeAddr)
        }

        val key = ECKey.fromPrivate(privateKeyBytes)
        val scriptPubKey = ScriptBuilder.createOutputScript(LegacyAddress.fromBase58(params, fromAddress))

        selected.forEach { utxo ->
            val outPoint = TransactionOutPoint(params, utxo.vout.toLong(), Sha256Hash.wrap(utxo.txid))
            val input = TransactionInput(params, tx, ByteArray(0), outPoint)
            tx.addInput(input)
        }

        tx.inputs.forEachIndexed { index, _ ->
            val sig = tx.calculateSignature(index, key, scriptPubKey, Transaction.SigHash.ALL, false)
            val inputScript = ScriptBuilder.createInputScript(sig, key)
            tx.inputs[index].scriptSig = inputScript
        }

        val raw = Utils.HEX.encode(tx.bitcoinSerialize())
        return BuildResult(rawTx = raw, feePaid = fee, changeValue = change)
    }

    fun estimateFee(inputCount: Int, outputCount: Int, feeRate: Long): Long {
        val size = inputCount * 148L + outputCount * 34L + 10L
        return size * feeRate
    }

    private fun selectUtxos(utxos: List<BtcUtxo>, amountSats: Long, feeRate: Long): List<BtcUtxo> {
        val sorted = utxos.sortedBy { it.value }
        val selected = mutableListOf<BtcUtxo>()
        var total = 0L
        for (utxo in sorted) {
            selected += utxo
            total += utxo.value
            val fee = estimateFee(selected.size, 2, feeRate)
            if (total >= amountSats + fee) return selected
        }
        throw IllegalArgumentException("Insufficient funds")
    }
}
