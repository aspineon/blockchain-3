package net.corda.workbench.assetTransfer.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.workbench.assetTransfer.contract.AssetContract
import net.corda.workbench.assetTransfer.contract.AssetContract.Companion.ID
import net.corda.workbench.assetTransfer.state.AssetState

@InitiatingFlow
@StartableByRPC
class CreateOfferFlow(private val linearId: UniqueIdentifier,
                      private val newcounterParty: Party,
                      private val inspector: Party,
                      private val appraiser: Party
                      ) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        val items = serviceHub.vaultService.queryBy<AssetState>(queryCriteria).states


        val inputStateAndRef = items.first()
        val _state = inputStateAndRef.state.data
        val outputstate = _state.AcceptAndValidateOffer(newcounterParty,inspector,appraiser,_state)
        val createCommand = Command(AssetContract.PlaceOffer(),outputstate.participants.map { it -> it.owningKey })
        val builder = TransactionBuilder(notary = notary)
                .addInputState(inputStateAndRef)
                .addOutputState(outputstate,ID)
                .addCommand(createCommand)

        val ptx = serviceHub.signInitialTransaction(builder)
        val sessions = (outputstate.participants - ourIdentity).map { initiateFlow(it) }.toSet()
        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))


        return subFlow(FinalityFlow(stx))

    }


}


@InitiatedBy(CreateOfferFlow::class)
class TransferFlowResponder(val flowSession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be a simple transaction" using (output is AssetState)

            }
        }
        subFlow(signedTransactionFlow)
    }
}