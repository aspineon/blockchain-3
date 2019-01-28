package net.corda.workbench.refrigeratedTransportation.flow.workbench

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic

import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker
import net.corda.reflections.workbench.TxnResult
import net.corda.workbench.refrigeratedTransportation.flow.CompleteFlow

/**
 * A wrapper compatible with Azure Blockchain Workbench, which needs a single list of params
 */
@InitiatingFlow
@StartableByRPC
class WorkbenchCompleteFlow(private val linearId: UniqueIdentifier) : FlowLogic<TxnResult>() {

    override val progressTracker: ProgressTracker = WorkbenchTracker().tracker()

    @Suspendable
    override fun call(): TxnResult {
        progressTracker.currentStep = WorkbenchTracker.RUNNING
        val txn = subFlow(CompleteFlow(linearId))
        progressTracker.currentStep = WorkbenchTracker.COMPLETED
        return buildWorkbenchTxn(txn, ourIdentity)
    }
}

