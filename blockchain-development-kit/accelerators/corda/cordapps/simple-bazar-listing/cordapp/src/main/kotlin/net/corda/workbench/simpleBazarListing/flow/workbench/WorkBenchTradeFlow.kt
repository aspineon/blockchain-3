package net.corda.workbench.simpleBazarListing.flow.workbench

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.reflections.workbench.TxnResult
import net.corda.workbench.simpleBazarListing.flow.CreateFlow
import net.corda.workbench.simpleBazarListing.flow.TradeFlow
import net.corda.workbench.simpleBazarListing.state.BazaarState

@InitiatingFlow
@StartableByRPC
class WorkbenchTradeFlow(private val linearId: UniqueIdentifier,
                         private val price:Double,
                         private val name:String

) : FlowLogic<TxnResult>() {

    @Suspendable
    override fun call(): TxnResult {

        val txn = subFlow(TradeFlow(linearId,price,name))
        return buildWorkbenchTxn(txn, ourIdentity)
    }
}

/**
 *
 */
@InitiatedBy(WorkbenchTradeFlow::class)
class WorkbenchTradeFlowResponder(val flowSession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        println("WorkbenchTradeFlowResponder: nothing to do - just print a message!")
    }
}