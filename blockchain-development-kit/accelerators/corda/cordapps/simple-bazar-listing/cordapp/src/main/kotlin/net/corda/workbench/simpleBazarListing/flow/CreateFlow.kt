package net.corda.workbench.simpleBazarListing.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.workbench.simpleBazarListing.contract.BazaarContract
import net.corda.workbench.simpleBazarListing.state.BazaarState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class CreateFlow(private val _state:BazaarState) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {

        val notary: Party = serviceHub.networkMapCache.notaryIdentities.first()
        val createCommand = Command(BazaarContract.Create(), listOf(ourIdentity.owningKey))
        val builder = TransactionBuilder(notary = notary)
                .addOutputState(_state, BazaarContract.ITEM_ID)
                .addCommand(createCommand)

        val stx = serviceHub.signInitialTransaction(builder)
        return  subFlow(FinalityFlow(stx))
    }

    @InitiatedBy(CreateFlow::class)
    class CreateFlowResponder(val flowSession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val output = stx.tx.outputs.single().data
                    "This must be a simple creation " using (output is BazaarState)
                }
            }
            subFlow(signedTransactionFlow)
        }

    }
}