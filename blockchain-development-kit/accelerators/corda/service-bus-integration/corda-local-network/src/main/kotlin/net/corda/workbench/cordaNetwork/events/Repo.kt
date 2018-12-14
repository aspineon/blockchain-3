package net.corda.workbench.cordaNetwork.events

import net.corda.core.identity.CordaX500Name
import net.corda.workbench.commons.event.EventStore
import net.corda.workbench.commons.event.Filter

class Repo(val es: EventStore) {


    /**
     * The full list of networks
     */
    fun networks(): List<NetworkInfo> {
        val networks = ArrayList<NetworkInfo>()
        es.retrieve(Filter(type = "NetworkCreated"))
                .forEach { event ->
                    val info = NetworkInfo(name = event.aggregateId!!)
                    networks.add(info)
                }
        return networks
    }

    /**
     * Full list of nodes for a network
     */
    fun nodes(network: String): List<NodeInfo> {
        val result = HashSet<NodeInfo>()
        es.retrieve(Filter(aggregateId = network, type = "NodesCreated"))
                .forEach { event ->
                    val nodes = event.payload["nodes"] as List<String>
                    result.addAll(nodes.map { it -> NodeInfo(it) })

                }
        return ArrayList(result).sortedBy { it.organisation }
    }
}

data class NetworkInfo(val name: String)

data class NodeInfo(val x500Name: String) {
    private val parts = CordaX500Name.parse(x500Name)
    val organisation = parts.organisation
    val country = parts.country
    val locality = parts.locality

}