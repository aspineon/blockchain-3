package net.corda.workbench.simpleBazarListing

import net.corda.core.contracts.ContractState
import java.security.PublicKey

fun keysFromParticipants(obligation: ContractState): Set<PublicKey> {
    return obligation.participants.map {
        it.owningKey
    }.toSet()
}