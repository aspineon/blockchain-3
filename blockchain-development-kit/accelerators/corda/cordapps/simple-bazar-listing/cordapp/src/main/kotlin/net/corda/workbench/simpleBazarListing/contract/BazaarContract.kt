package net.corda.workbench.simpleBazarListing.contract

import net.corda.workbench.simpleBazarListing.state.BazaarState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import net.corda.workbench.simpleBazarListing.state.TradeState

import java.security.PublicKey


class BazaarContract : Contract {
    companion object {
        @JvmStatic
        val ITEM_ID = "net.corda.workbench.simpleBazarListing.contract.BazaarContract"
    }

    override fun verify(tx: LedgerTransaction) {
        val itemCommand = tx.commands.requireSingleCommand<Commands>()
        val setOfSigners = itemCommand.signers.toSet()

        when (itemCommand.value) {
            is Create -> verifyCreate(tx, setOfSigners)
            is FinalizeTrade-> verifyFinalizeTrade(tx, setOfSigners)
            is Trade -> verifyTrade(tx, setOfSigners)
            else -> throw IllegalArgumentException("Unrecognised command.")
        }
    }

    private fun verifyCreate(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        "No inputs should be consumed when creating an item." using (tx.inputStates.isEmpty())
        "Only one item state should be created." using (tx.outputStates.size == 1)
        val _item = tx.outputStates.single() as BazaarState
        "A balance must be higher than zero." using (_item.balancePartyA>0&&_item.balancePartyB>0)
    }

   private fun verifyTrade(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
      //  "No inputs should be consumed when creating an item." using (tx.inputStates.size ==1)
        "Only one item state should be created." using (tx.outputStates.size == 1)
        val _item = tx.outputStates.single() as TradeState
        "Buyer balance must be higher than the object price" using (_item.itemPrice!=0.0)
   }

    private fun verifyFinalizeTrade(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
       "Inputs should be consumed when creating an item." using (tx.inputStates.size ==1)
        "Only one item state should be created." using (tx.outputStates.size == 1)
        val _item = tx.outputStates.single() as BazaarState

    }

    interface Commands : CommandData
        class Create : TypeOnlyCommandData(), Commands
        class Trade : TypeOnlyCommandData(), Commands
        class FinalizeTrade : TypeOnlyCommandData(), Commands
}
