package net.corda.workbench.assetTransfer.contract

import net.corda.workbench.assetTransfer.keysFromParticipants
import net.corda.workbench.assetTransfer.state.AssetState
import net.corda.workbench.assetTransfer.state.StateType
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey
import java.time.Instant


class AssetContract : Contract {
    companion object {
        @JvmStatic
        val ID = "net.corda.workbench.assetTransfer.contract.AssetContract"
    }

    override fun verify(tx: LedgerTransaction) {
        val itemCommand = tx.commands.requireSingleCommand<Commands>()
        val setOfSigners = itemCommand.signers.toSet()

        when (itemCommand.value) {
            is Create -> verifyCreate(tx, setOfSigners)
            is PlaceOffer ->verifyOffer(tx,setOfSigners)
            is ValidateOffer->verifyValidateOffer(tx,setOfSigners)
            else -> throw IllegalArgumentException("Unrecognised command.")
        }
    }

    private fun verifyCreate(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        "No inputs should be consumed when creating an item." using (tx.inputStates.isEmpty())
        "Only one item state should be created." using (tx.outputStates.size == 1)
        val _asset = tx.outputStates.single() as AssetState
        "A new item must have value" using (_asset.value!=0.0||_asset.value!=null)
        "Item must be set on active" using (_asset.state==StateType.active)
    }

    private fun verifyOffer(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        "Only one item state should be created." using (tx.outputStates.size == 1)
       // "Incorrect number of signers." using (signers.size == 2)
        val _asset = tx.outputStates.single() as AssetState
        "Item must be set on active" using (_asset.state==StateType.Accepted)
    }

    private fun verifyValidateOffer(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        "Only one item state should be created." using (tx.outputStates.size == 1)

       //  "Incorrect number of signers." using (signers.size == 2)
        val _asset = tx.outputStates.single() as AssetState

    }


    interface Commands : CommandData
        class Create : TypeOnlyCommandData(), Commands
        class PlaceOffer : TypeOnlyCommandData(), Commands
        class ValidateOffer : TypeOnlyCommandData(), Commands
}
