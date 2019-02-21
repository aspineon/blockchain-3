package net.corda.workbench.assetTransfer.client



import net.corda.workbench.assetTransfer.state.AssetState
import net.corda.client.rpc.CordaRPCClient
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.loggerFor
import net.corda.workbench.assetTransfer.flow.CreateFlow
import net.corda.workbench.assetTransfer.flow.CreateOfferFlow
import net.corda.workbench.assetTransfer.state.StateType
import org.slf4j.Logger


fun main(args: Array<String>) {
    ExampleClientRPC().main(args)
}

private class ExampleClientRPC {
    companion object {
        val logger: Logger = loggerFor<ExampleClientRPC>()
        private fun logState(state: StateAndRef<AssetState>) = logger.info("{}", state.state.data)
    }

    fun main(args: Array<String>) {
        require(args.size == 1) { "Usage: ExampleClientRPC <node address>" }
        val nodeAddress = NetworkHostAndPort.parse(args[0])
        val client = CordaRPCClient(nodeAddress)


        val proxy = client.start("user1", "test").proxy

        val otherpartyName = CordaX500Name("PartyA", "London", "GB")
        val second = proxy.wellKnownPartyFromX500Name(otherpartyName)

        val newcouterPartyName = CordaX500Name("PartyB", "New York", "US")
        val newcouterParty = proxy.wellKnownPartyFromX500Name(newcouterPartyName)

        val InspecorName = CordaX500Name("PartyC", "Paris", "FR")
        val Inspector = proxy.wellKnownPartyFromX500Name(InspecorName)

        val appName = CordaX500Name("PartyD", "Milan", "IT")
        val Appraiser = proxy.wellKnownPartyFromX500Name(appName)

        val _item = AssetState(100.0,StateType.active,second!!,null,null,null)

        val signedTx = proxy.startTrackedFlowDynamic(CreateFlow::class.java,_item)
                .returnValue
                .get()

        val sTx = proxy.startTrackedFlowDynamic(CreateOfferFlow::class.java,_item.linearId,newcouterParty,Inspector,Appraiser)
                .returnValue
                .get()

        val (snapshot, updates) = proxy.vaultTrack(AssetState::class.java)
        val vaultSnapshot = proxy.vaultQueryBy<AssetState>(
                QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED))


    }
}
