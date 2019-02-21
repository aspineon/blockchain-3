package net.corda.workbench.refrigeratedTransportation.flow.workbench

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.utilities.ProgressTracker
import net.corda.reflections.workbench.TxnResult
import net.corda.workbench.refrigeratedTransportation.Transfer
import net.corda.workbench.refrigeratedTransportation.flow.TransferResponsibilityFlow

/**
 * A wrapper compatible with Azure Workbench, which
 * needs a single list of params and a TxnResult / TxnResultTyped
 * return type
 */
@InitiatingFlow
@StartableByRPC
class WorkbenchTransferFlow(private val linearId: UniqueIdentifier,
                            private val newCounterparty: Party) : FlowLogic<TxnResult>() {

    override val progressTracker: ProgressTracker = WorkbenchTracker().tracker()


    @Suspendable
    override fun call(): TxnResult {
        progressTracker.currentStep = WorkbenchTracker.RUNNING
        val transfer = Transfer(newCounterparty = newCounterparty)
        val txn = subFlow(TransferResponsibilityFlow(linearId, transfer))
        progressTracker.currentStep = WorkbenchTracker.COMPLETED
        return buildWorkbenchTxn(txn, ourIdentity)

    }
}
