package net.corda.workbench.cordaNetwork.events

import net.corda.core.identity.CordaX500Name
import net.corda.workbench.commons.event.EventStore
import net.corda.workbench.commons.event.Filter

class Repo(val es: EventStore) {


    /**
     * The full list of networks
     */
    fun networks(): List<NetworkInfo> {
        val networks = HashMap<String, MutableMap<String, String>>()
        es.retrieve()
                .forEach { ev ->
                    when {
                        ev.type == "NetworkCreated" -> {
                            networks[ev.aggregateId!!] = mutableMapOf("name" to ev.aggregateId!!)
                        }
                        ev.type == "NetworkStarted" -> {
                            val data = networks[ev.aggregateId]
                            if (data != null) {
                                data["status"] = "Running"
                            }
                        }
                        ev.type == "NetworkStopped" -> {
                            val data = networks[ev.aggregateId]
                            if (data != null) {
                                data["status"] = "Stopped"
                            }
                        }
                        else -> { /* dont care */
                        }
                    }
                }
        return networks
                .values
                .map { NetworkInfo.fromMap(it) }
                .sortedBy { it.name }
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

data class NetworkInfo(val name: String, val status: String) {
    companion object {
        fun fromMap(data: Map<String, String>): NetworkInfo {
            return NetworkInfo(name = data["name"]!!,
                    status = data.getOrDefault("status", "Never Started"))
        }
    }
}


data class NodeInfo(val x500Name: String) {
    private val parts = CordaX500Name.parse(x500Name)
    val organisation = parts.organisation
    val country = parts.country
    val locality = parts.locality

}