package net.corda.workbench.simpleBazarListing.flow

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
import net.corda.workbench.simpleBazarListing.contract.ItemContract
import net.corda.workbench.simpleBazarListing.contract.ItemContract.Companion.ITEM_ID
import net.corda.workbench.simpleBazarListing.state.BazaarState
import net.corda.workbench.simpleBazarListing.state.TradeState

@InitiatingFlow
@StartableByRPC
class TradeFlow(private val linearId: UniqueIdentifier,private val price:Double,private val name:String) : FlowLogic<SignedTransaction>() {



    @Suspendable
    override fun call(): SignedTransaction {

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        val items = serviceHub.vaultService.queryBy<BazaarState>(queryCriteria).states


        val inputStateAndRef = items.first()
        val _state = inputStateAndRef.state.data
        val _tradeState = TradeState(price,name,_state.PartyA,_state.PartyB,_state.bazaarMaintainer)
        _state.BuyAndSell(price)
        val createCommand = Command(ItemContract.FinalizeTrade(), listOf(ourIdentity.owningKey))
        val builder = TransactionBuilder(notary = notary)
                .addInputState(inputStateAndRef)
                .addOutputState(_state, ITEM_ID)
                .addCommand(createCommand)


        val stx = serviceHub.signInitialTransaction(builder)
        return  subFlow(FinalityFlow(stx))


    }


}


@InitiatedBy( TradeFlow::class)
class  TradeFlowFlowResponder(val flowSession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be a simple transaction" using (output is BazaarState)
            }
        }
        subFlow(signedTransactionFlow)
    }
}