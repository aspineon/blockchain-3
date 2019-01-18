package net.corda.workbench.simpleBazarListing.state

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class TradeState(val itemPrice:Double,
                      val itemName:String,
                      val bazaarMaintainer:Party,
                      val partyA :Party,
                      val partyB :Party,
                      override val participants: List<Party> = listOf(bazaarMaintainer,partyA,partyB),
                      override val linearId: UniqueIdentifier = UniqueIdentifier()):
        LinearState {




}