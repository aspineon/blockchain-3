package net.corda.workbench.chat.flow.workbench

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.reflections.annotations.Description
import net.corda.reflections.annotations.fsm.FSMActionsForState


@InitiatingFlow
@StartableByRPC
@Description("The available flows based on the current FSM state")
@FSMActionsForState()
class FSMActionsForStateFlow(private val linearId: UniqueIdentifier) : FlowLogic<List<String>>() {

    @Suspendable
    override fun call(): List<String> {
        val state = subFlow(FSMCurrentStateFlow(linearId))

        return when (state) {
            "NotStarted" -> listOf(WorkbenchStartFlow::class.simpleName!!)
            "Chatting" -> listOf(WorkbenchChatFlow::class.simpleName!!, WorkbenchEndFlow::class.simpleName!!)
            else -> emptyList()
        }
    }
}
