package net.corda.workbench.simpleBazarListing.state



import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class BazaarState(val PartyA: Party,
                       var balancePartyA:Double,
                       val PartyB:Party,
                       var balancePartyB:Double,
                       val bazaarMaintainer:Party,
                       override val participants: List<Party> = listOf(bazaarMaintainer),
                       override val linearId: UniqueIdentifier = UniqueIdentifier()):
        LinearState {

fun UpdateBalance(price:Double,seller:Party, buyer:Party,_state:BazaarState):BazaarState
{
  return copy(PartyA = seller,balancePartyA = _state.balancePartyA+price,PartyB = buyer,balancePartyB = _state.balancePartyB-price,
          bazaarMaintainer =_state.bazaarMaintainer)

}
}
