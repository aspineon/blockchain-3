package net.corda.workbench.assetTransfer.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.workbench.assetTransfer.contract.AssetContract
import net.corda.workbench.assetTransfer.state.AssetState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.workbench.assetTransfer.contract.AssetContract.Companion.ID

@InitiatingFlow
@StartableByRPC
class CreateFlow(private val _state:AssetState) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {

        val notary: Party = serviceHub.networkMapCache.notaryIdentities.first()
        val createCommand = Command(AssetContract.Create(), listOf(ourIdentity.owningKey))
        val builder = TransactionBuilder(notary = notary)
                .addOutputState(_state,ID)
                .addCommand(createCommand)

        val stx = serviceHub.signInitialTransaction(builder)
        val ftx = subFlow(FinalityFlow(stx))


        return ftx
    }

    @InitiatedBy(CreateFlow::class)
    class CreateFlowResponder(val flowSession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val output = stx.tx.outputs.single().data
                    "This must be a simple creation " using (output is AssetState)
                }
            }
            subFlow(signedTransactionFlow)
        }

    }
}