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

fun BuyAndSell(price:Double)
{
    this.balancePartyA =this.balancePartyA+price
    this.balancePartyB=this.balancePartyB-price

}
}
