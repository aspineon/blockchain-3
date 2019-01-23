package net.corda.workbench.simpleBazarListing.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.workbench.simpleBazarListing.contract.BazaarContract
import net.corda.workbench.simpleBazarListing.state.BazaarState
import net.corda.workbench.simpleBazarListing.state.TradeState



@InitiatingFlow
@StartableByRPC
class FinalizeTradeFlow( val _tradeState:TradeState) : FlowLogic<SignedTransaction>() {



    @Suspendable
    override fun call(): SignedTransaction {
        val notary: Party = serviceHub.networkMapCache.notaryIdentities.first()
        val FinalizeCommand = Command(BazaarContract.Trade(),_tradeState.participants.map { it -> it.owningKey })
        val builder = TransactionBuilder(notary = notary)
                .addOutputState(_tradeState, BazaarContract.ITEM_ID)
                .addCommand(FinalizeCommand)


        val ptx = serviceHub.signInitialTransaction(builder)
        val sessions = (_tradeState.participants - ourIdentity).map { initiateFlow(it) }.toSet()
        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))
        return subFlow(FinalityFlow(stx))
    }
}


@InitiatedBy(FinalizeTradeFlow::class)
class  FinalizeTradeFlowFlowResponder(val flowSession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data

            }
        }
        subFlow(signedTransactionFlow)
    }
}