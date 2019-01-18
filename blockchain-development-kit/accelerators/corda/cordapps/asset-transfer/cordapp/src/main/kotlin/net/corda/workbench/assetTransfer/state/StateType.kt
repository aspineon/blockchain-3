package net.corda.workbench.assetTransfer.state

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class StateType {

    active,
    offerPlaced,
    inspected,
    Accepted
}