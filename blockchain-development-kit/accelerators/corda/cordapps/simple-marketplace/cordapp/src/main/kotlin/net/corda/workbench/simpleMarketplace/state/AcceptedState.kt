package net.corda.workbench.simpleMarketplace.state

import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
/**
 * AcceptedState contains the object of the transaction .
 *
 * See https://github.com/Azure-Samples/blockchain/blob/master/blockchain-workbench/application-and-smart-contract-samples/simple-marketplace/ethereum/SimpleMarketplace.sol
 * for reference model under Ethereum.
 */
@CordaSerializable
class AcceptedState(var owner: Party,
                    var buyer: Party,
                    var item: AvailableItem,
                    var offeredPrice: Double,
                    override val linearId: UniqueIdentifier = UniqueIdentifier()) : ContractState, LinearState {


    override val participants: List<Party> get() = buildParticipants()

    fun buildParticipants(): List<Party> {
        val result = mutableSetOf(owner, buyer)

        return result.toList()
    }

    /**
     * This function allow to change the item status
     * to approved*
     */

    fun ChangeStateTypeToApproved() {
        this.item._state = StateType.approved
    }
}
