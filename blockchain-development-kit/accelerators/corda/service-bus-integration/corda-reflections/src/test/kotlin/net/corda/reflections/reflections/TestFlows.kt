package net.corda.reflections.reflections

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.TransactionState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.CoreTransaction
import net.corda.reflections.annotations.Description
import net.corda.reflections.annotations.fsm.FSMActionsForState
import net.corda.reflections.annotations.fsm.FSMCurrentState
import net.corda.reflections.annotations.fsm.FSMStates
import java.lang.RuntimeException

/**
 * Various patterns needed to test reflections
 */


@InitiatingFlow
@StartableByRPC
class SimpleFlow(private val data: String) : FlowLogic<String>() {

    @Suspendable
    override fun call(): String {
        if (data == "bad") throw  RuntimeException("Forced an Exception")
        return data.toUpperCase()
    }
}

@InitiatingFlow
class NotRpcFlow() : FlowLogic<String>() {

    @Suspendable
    override fun call(): String {
        return "result"
    }
}

@StartableByRPC
class NotInitiatingFlow() : FlowLogic<String>() {

    @Suspendable
    override fun call(): String {
        return "result"
    }
}

@InitiatingFlow
@StartableByRPC
class MultipleConstructorFlow(val p1: String, val p2: Int) : FlowLogic<String>() {
    constructor(params: TwoParams) : this(params.name, params.age)

    @Suspendable
    override fun call(): String {
        return p1.toUpperCase()
    }
}


@InitiatingFlow
@StartableByRPC
@Description("Hello World")
class FlowWithAnnotations() : FlowLogic<String>() {

    @Suspendable
    override fun call(): String {
        return "result"
    }
}

@InitiatingFlow
@StartableByRPC
@FSMStates
class FSMStatesFlow() : FlowLogic<List<String>>() {

    @Suspendable
    override fun call(): List<String> {
        return States.values().map { it.name }
    }
}

@InitiatingFlow
@StartableByRPC
@FSMCurrentState
class FSMCurrentStateFlow(private val linearId: UniqueIdentifier) : FlowLogic<String>() {

    @Suspendable
    override fun call(): String {
        return "StateA"
    }
}


@InitiatingFlow
@StartableByRPC
@FSMActionsForState
class FSMActionsForStateFlow(private val state: String) : FlowLogic<List<String>>() {

    @Suspendable
    override fun call(): List<String> {
        return listOf("Flow1", "Flow2")
    }
}


class FakeTransaction : CoreTransaction() {
    override val id: SecureHash
        get() = SecureHash.zeroHash
    override val inputs: List<StateRef>
        get() = emptyList()
    override val notary: Party?
        get() = null
    override val outputs: List<TransactionState<ContractState>>
        get() = emptyList()

}

/**
 * The possible state
 */
enum class States {
    StateA,
    StateB
}
