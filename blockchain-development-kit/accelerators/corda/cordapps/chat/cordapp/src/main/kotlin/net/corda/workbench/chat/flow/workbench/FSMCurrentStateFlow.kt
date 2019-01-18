package net.corda.workbench.chat.flow.workbench

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.reflections.annotations.Description
import net.corda.reflections.annotations.fsm.FSMCurrentState
import net.corda.workbench.chat.Message


@InitiatingFlow
@StartableByRPC
@Description("The current FSM state")
@FSMCurrentState
class FSMCurrentStateFlow(private val linearId: UniqueIdentifier) : FlowLogic<String>() {

    @Suspendable
    override fun call(): String {
        // Retrieve the shipment from the vault.
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        val items = serviceHub.vaultService.queryBy<Message>(queryCriteria).states

        return if (items.isEmpty()) {
            "NotStarted"
        } else {
            if (items.single().state.data.message.equals("bye", ignoreCase = true)) {
                States.Completed.name
            } else {
                States.Chatting.name
            }
        }
    }
}
