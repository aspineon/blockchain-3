package net.corda.workbench.simpleBazarListing.client

import net.corda.workbench.simpleBazarListing.flow.CreateFlow

import net.corda.workbench.simpleBazarListing.state.BazaarState
import net.corda.client.rpc.CordaRPCClient
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.loggerFor
import net.corda.workbench.simpleBazarListing.flow.TradeFlow
import net.corda.workbench.simpleBazarListing.state.TradeState
import org.slf4j.Logger


fun main(args: Array<String>) {
    ExampleClientRPC().main(args)
}

private class ExampleClientRPC {
    companion object {
        val logger: Logger = loggerFor<ExampleClientRPC>()
        private fun logState(state: StateAndRef<BazaarState>) = logger.info("{}", state.state.data)
    }

    fun main(args: Array<String>) {
        require(args.size == 1) { "Usage: ExampleClientRPC <node address>" }
        val nodeAddress = NetworkHostAndPort.parse(args[0])
        val client = CordaRPCClient(nodeAddress)


        val proxy = client.start("user1", "test").proxy

        val otherpartyName = CordaX500Name("PartyA", "London", "GB")
        val maintainer = proxy.wellKnownPartyFromX500Name(otherpartyName)

        val observerName = CordaX500Name("PartyC", "Paris", "FR")
        val partyA = proxy.wellKnownPartyFromX500Name(observerName)

        val newcouterPartyName = CordaX500Name("PartyB", "New York", "US")
        val partyB = proxy.wellKnownPartyFromX500Name(newcouterPartyName)

        val _item = BazaarState(partyA!!,100000.00,partyB!!,200000.11,maintainer!!)

        val signedTx = proxy.startTrackedFlowDynamic(CreateFlow::class.java,_item)
                .returnValue
                .get()

        val sTx = proxy.startTrackedFlowDynamic(TradeFlow::class.java,_item.linearId,100.00,"Item01")
                .returnValue
                .get()



        val (snapshot, updates) = proxy.vaultTrack(BazaarState::class.java)
        val vaultSnapshot = proxy.vaultQueryBy<BazaarState>(
                QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED))
    }
}
