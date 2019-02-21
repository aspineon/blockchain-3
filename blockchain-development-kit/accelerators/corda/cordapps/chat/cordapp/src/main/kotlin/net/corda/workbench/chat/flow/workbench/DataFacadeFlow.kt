package net.corda.workbench.chat.flow.workbench

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.reflections.annotations.Description
import net.corda.reflections.annotations.facade.DataFacade
import net.corda.reflections.annotations.fsm.FSMCurrentState
import net.corda.reflections.workbench.ContractProperty
import net.corda.workbench.chat.Message


@InitiatingFlow
@StartableByRPC
@Description("The current state in format needed for Workbench")
@DataFacade
class DataFacadeFlow(private val linearId: UniqueIdentifier) : FlowLogic<List<ContractProperty>>() {

    @Suspendable
    override fun call(): List<ContractProperty> {
        // Retrieve the shipment from the vault.
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        val items = serviceHub.vaultService.queryBy<Message>(queryCriteria).states

        return if (items.isEmpty()) {
            emptyList()
        } else {
            buildContractPropertyList(message = items.single().state.data)
        }
    }
}
