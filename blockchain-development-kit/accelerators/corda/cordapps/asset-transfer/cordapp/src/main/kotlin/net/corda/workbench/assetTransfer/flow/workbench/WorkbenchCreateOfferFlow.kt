package net.corda.workbench.assetTransfer.flow.workbench

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.reflections.workbench.TxnResult
import net.corda.workbench.assetTransfer.flow.CreateOfferFlow
import net.corda.workbench.assetTransfer.state.AssetState

@InitiatingFlow
@StartableByRPC
class WorkbenchCreateOfferFlow(private val linearId: UniqueIdentifier,
                               private val newcounterParty: Party,
                          private val Inspector: Party,
                          private val Appraiser: Party
) : FlowLogic<TxnResult>() {

    @Suspendable
    override fun call(): TxnResult {



        val txn = subFlow(CreateOfferFlow(linearId,newcounterParty,Inspector,Appraiser))
        return buildWorkbenchTxn(txn, ourIdentity)
    }
}

/**
 *
 */
@InitiatedBy(WorkbenchCreateOfferFlow::class)
class WorkbenchCreateOfferFlowResponder(val flowSession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        println("WorkbenchCreateFlowResponder: nothing to do - just print a message!")
    }
}