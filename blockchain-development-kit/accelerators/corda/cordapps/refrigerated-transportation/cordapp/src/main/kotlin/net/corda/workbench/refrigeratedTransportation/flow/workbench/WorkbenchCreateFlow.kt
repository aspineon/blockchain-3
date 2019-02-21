package net.corda.workbench.refrigeratedTransportation.flow.workbench

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.utilities.ProgressTracker
import net.corda.reflections.workbench.TxnResult
import net.corda.workbench.refrigeratedTransportation.Shipment
import net.corda.workbench.refrigeratedTransportation.flow.CreateFlow

/**
 * A wrapper compatible with Azure Workbench, which
 * needs a single list of params and a TxnResult / TxnResultTyped
 * return type
 */
@InitiatingFlow
@StartableByRPC
class WorkbenchCreateFlow(private val linearId: UniqueIdentifier,
                          private val device: Party,
                          private val supplyChainOwner: Party,
                          private val supplyChainObserver: Party,
                          private val minHumidity: Int,
                          private val maxHumidity: Int,
                          private val minTemperature: Int,
                          private val maxTemperature: Int) : FlowLogic<TxnResult>() {


    override val progressTracker: ProgressTracker = WorkbenchTracker().tracker()

    @Suspendable
    override fun call(): TxnResult {

        progressTracker.currentStep = WorkbenchTracker.RUNNING

        val state = Shipment(owner = ourIdentity,
                device = device,
                supplyChainObserver = supplyChainObserver,
                supplyChainOwner = supplyChainOwner,
                minHumidity = minHumidity,
                maxHumidity = maxHumidity,
                minTemperature = minTemperature,
                maxTemperature = maxTemperature,
                linearId = linearId)

        val txn = subFlow(CreateFlow(state))
        progressTracker.currentStep = WorkbenchTracker.COMPLETED

        return buildWorkbenchTxn(txn, ourIdentity)

    }
}

