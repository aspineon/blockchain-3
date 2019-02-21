package net.corda.workbench.chat.flow.workbench

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.reflections.annotations.Description
import net.corda.reflections.annotations.fsm.FSMStates

/**
 * The possible state
 */
enum class States  {
    Chatting,
    Completed
}


@InitiatingFlow
@StartableByRPC
@Description("The full set of possible FSM states")
@FSMStates
class FSMStatesFlow() : FlowLogic<List<String>>() {

    @Suspendable
    override fun call(): List<String> {
        return States.values().map { it.name }
    }
}
