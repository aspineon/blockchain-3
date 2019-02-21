package net.corda.workbench.assetTransfer.flow.workbench

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.reflections.workbench.TxnResult
import net.corda.workbench.assetTransfer.flow.CreateFlow
import net.corda.workbench.assetTransfer.state.AssetState
import net.corda.workbench.assetTransfer.state.StateType

@InitiatingFlow
@StartableByRPC
class WorkbenchCreateFlow(private val buyer: Party,
                          private val seller: Party,
                          private val value: Double
) : FlowLogic<TxnResult>() {

    @Suspendable
    override fun call(): TxnResult {

        val state = AssetState(
                value = value,
                Buyer = buyer,
                Seller = seller,
                state=  StateType.active,
                Inspector = null,
                Appraiser = null,
                linearId = UniqueIdentifier()
        )

        val txn = subFlow(CreateFlow(state))
        return buildWorkbenchTxn(txn, ourIdentity)
    }
}

/**
 *
 */
@InitiatedBy(WorkbenchCreateFlow::class)
class WorkbenchCreateFlowResponder(val flowSession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        println("WorkbenchCreateFlowResponder: nothing to do - just print a message!")
    }
}