package net.corda.workbench.simpleBazarListing.state

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class TradeState(val itemPrice:Double,
                      val itemName:String,
                      val buyer :Party,
                      val seller :Party,
                      override val participants: List<Party> = listOf(buyer,seller),
                      override val linearId: UniqueIdentifier = UniqueIdentifier()):
        LinearState {




}