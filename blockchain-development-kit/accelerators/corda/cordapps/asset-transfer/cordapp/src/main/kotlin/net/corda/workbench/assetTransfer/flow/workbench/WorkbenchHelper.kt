package net.corda.workbench.assetTransfer.flow.workbench

import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.reflections.workbench.ContractProperty
import net.corda.reflections.workbench.TxnResult
import net.corda.workbench.assetTransfer.state.AssetState

fun buildWorkbenchTxn(txn: SignedTransaction, me: Party): TxnResult {

    val txnHash = txn.coreTransaction.id

    val output = txn.coreTransaction.outputsOfType<AssetState>().single()
    val otherParties = output.participants - me
    return TxnResult(txnHash = txnHash.toString(),
            owner = me.name,
            otherParties = otherParties.map { it.name},
            contractProperties = buildContractPropertyList(output))
}

fun buildContractPropertyList(_item: AssetState): List<ContractProperty> {
    val result = ArrayList<ContractProperty>()
    result.add(ContractProperty("value", _item.value))
    result.add(ContractProperty("state", _item.state))
    result.add(ContractProperty("Buyer", _item.Buyer))
    result.add(ContractProperty("Seller", _item.Seller))
    result.add(ContractProperty("Appraiser", _item.Appraiser))
    result.add(ContractProperty("Inspector", _item.Inspector))
    result.add(ContractProperty("linearId", _item.linearId))

    return result
}