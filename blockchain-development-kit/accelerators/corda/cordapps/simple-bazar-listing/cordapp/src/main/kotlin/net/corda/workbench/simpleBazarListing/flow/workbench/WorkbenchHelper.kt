package net.corda.workbench.simpleBazarListing.flow.workbench

import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.reflections.workbench.ContractProperty
import net.corda.reflections.workbench.TxnResult
import net.corda.workbench.simpleBazarListing.state.BazaarState

fun buildWorkbenchTxn(txn: SignedTransaction, me: Party): TxnResult {

    val txnHash = txn.coreTransaction.id

    val output = txn.coreTransaction.outputsOfType<BazaarState>().single()
    val otherParties = output.participants - me
    return TxnResult(txnHash = txnHash.toString(),
            owner = me.name,
            otherParties = otherParties.map { it.name},
            contractProperties = buildContractPropertyList(output))
}

fun buildContractPropertyList(_item: BazaarState): List<ContractProperty> {
    val result = ArrayList<ContractProperty>()
    result.add(ContractProperty("PartyA", _item.PartyA))
    result.add(ContractProperty("balancePartyA", _item.balancePartyA))
    result.add(ContractProperty("PartyB", _item.PartyB))
    result.add(ContractProperty("balancePartyB", _item.balancePartyB))
    result.add(ContractProperty("bazaarMaintainer", _item.bazaarMaintainer))
    result.add(ContractProperty("linearId", _item.linearId))

    return result
}