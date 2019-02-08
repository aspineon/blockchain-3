package net.corda.workbench.transactionBuilder.events

import net.corda.workbench.commons.event.Event
import net.corda.workbench.commons.event.EventStore
import net.corda.workbench.commons.event.Filter
import net.corda.workbench.transactionBuilder.CordaAppConfig
import java.util.*
import kotlin.collections.HashSet

class Repo(val es: EventStore) {

    /**
     * The full list of networks
     */
    fun joinedNetworks(): List<String> {
        val networks = HashSet<String>()
        es.retrieve(Filter(type = "NetworkJoined"))
                .forEach { ev ->
                    networks.add(ev.aggregateId!!)
                }
        return networks.toList()

    }

    /**
     * Full list of nodes for a network
     */
    fun nodes(network: String): List<String> {
        val result = ArrayList<String>()
        es.retrieve(Filter(aggregateId = network, type = "NetworkJoined"))
                .forEach { event ->
                    val nodes = event.payload["nodes"] as List<String>
                    result.clear()
                    result.addAll(nodes)

                }
        return result.sorted()
    }

    // reduce events to find the current port
    fun agentPort(network: String): Int {
        return es.retrieve(Filter(aggregateId = network))
                .fold(0) { port, event ->
                    when {
                        @Suppress("UNCHECKED_CAST")
                        event.type == "AgentStarted" ->
                            event.payload["port"] as Int
                        event.type == "AgentStopped" -> 0
                        else -> port
                    }
                }

    }

    fun cordaAppConfig(network: String, appname: String): CordaAppConfig? {
        return allCordaAppConfigs(network).singleOrNull() { it.name == appname }
    }

    fun allCordaAppConfigs(network: String): List<CordaAppConfig> {

        // latest event for each app - should really be a common pattern in the repo
        var events = ArrayList<Event>()
        es.retrieve(Filter(aggregateId = network, type = "CordaAppDeployed"))
                .groupBy { it.payload["appname"] }
                .values.forEach { events.add(it.last()) }

        return events.map { it ->
            CordaAppConfig(id = UUID.fromString(it.payload["appId"] as String),
                    name = it.payload["appname"] as String,
                    scannablePackages = it.payload["scannablePackages"] as List<String>)
        }
    }


}