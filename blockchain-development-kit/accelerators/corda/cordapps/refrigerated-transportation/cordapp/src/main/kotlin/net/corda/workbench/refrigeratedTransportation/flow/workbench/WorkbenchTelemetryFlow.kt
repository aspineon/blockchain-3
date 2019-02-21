package net.corda.workbench.refrigeratedTransportation.flow.workbench

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker
import net.corda.reflections.workbench.TxnResult
import net.corda.workbench.refrigeratedTransportation.Telemetry
import net.corda.workbench.refrigeratedTransportation.flow.IngestTelemetryFlow

/**
 * A wrapper compatible with Azure Workbench, which
 * needs a single list of params and a TxnResult / TxnResultTyped
 * return type
 */
@InitiatingFlow
@StartableByRPC
class WorkbenchTelemetryFlow(private val linearId: UniqueIdentifier,
                             private val temperature: Int,
                             private val humidity: Int) : FlowLogic<TxnResult>() {

    override val progressTracker: ProgressTracker = WorkbenchTracker().tracker()


    @Suspendable
    override fun call(): TxnResult {
        progressTracker.currentStep = WorkbenchTracker.RUNNING
        val telemetry = Telemetry(temperature = temperature, humidity = humidity)
        val txn = subFlow(IngestTelemetryFlow(linearId, telemetry))
        progressTracker.currentStep = WorkbenchTracker.COMPLETED
        return buildWorkbenchTxn(txn, ourIdentity)
    }
}
