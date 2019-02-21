package net.corda.workbench.simpleBazarListing.flow.workbench

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.reflections.workbench.TxnResult
import net.corda.workbench.simpleBazarListing.flow.CreateFlow
import net.corda.workbench.simpleBazarListing.state.BazaarState

@InitiatingFlow
@StartableByRPC
class WorkbenchCreateFlow(private val partyA: Party,
                          private val partyB: Party,
                          private val bazaarMaintainer: Party,
                          private val balancePartyA: Double,
                          private val balancePartyB: Double
) : FlowLogic<TxnResult>() {

    @Suspendable
    override fun call(): TxnResult {

        val state = BazaarState(
                PartyA = partyA,
                PartyB=partyB,
                balancePartyA = balancePartyA,
                balancePartyB = balancePartyB,
                bazaarMaintainer = bazaarMaintainer,
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