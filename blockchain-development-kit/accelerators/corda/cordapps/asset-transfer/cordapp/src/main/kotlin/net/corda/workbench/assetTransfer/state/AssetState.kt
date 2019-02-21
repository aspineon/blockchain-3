package net.corda.workbench.assetTransfer.state


import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class AssetState(val value: Double,
                      var state: StateType =  StateType.active,
                      val Seller: Party,
                      val Buyer :Party?,
                      val Appraiser:Party?,
                      val Inspector: Party?,
                      override val linearId: UniqueIdentifier = UniqueIdentifier()):
        LinearState {

    override val participants: List<Party> get() = buildParticipants()

    fun buildParticipants(): List<Party> {
        val result = mutableSetOf(Seller)
        if (Buyer != null) result.add(Buyer)
        if (Appraiser != null) result.add(Appraiser)
        if (Inspector != null) result.add(Inspector)
        return result.toList()
    }

//    fun PlaceOffer(newCounterParty: Party,_state:AssetState): AssetState {
//        return copy(value = _state.value,state = StateType.offerPlaced, Seller = _state.Seller,Buyer = newCounterParty)
//    }

    fun AcceptAndValidateOffer(newCounterParty: Party,Inspector: Party?, Appraiser: Party?,_state:AssetState): AssetState {
        return copy(value = _state.value,state = StateType.Accepted, Inspector = Inspector,Appraiser = Appraiser,
                Seller = _state.Seller,Buyer = newCounterParty)
    }
}
