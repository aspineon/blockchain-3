package net.corda.workbench.simpleBazarListing

import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.driver.DriverParameters
import net.corda.testing.driver.driver
import net.corda.testing.node.User

/**
 * This file is exclusively for being able to run your nodes through an IDE.
 * Do not use in a production environment.
 */
fun main(args: Array<String>) {
    val user = User("user1", "test", permissions = setOf("ALL"))
    driver(DriverParameters(waitForAllNodesToFinish = true)) {
        val nodeFutures = listOf(
                startNode(
                        providedName = CordaX500Name("ContosoLtd", "Seatle", "US"),
                        customOverrides = mapOf("rpcSettings.address" to "localhost:10021",
                                "rpcSettings.adminAddress" to "localhost:10022",
                                "sshd.port" to 10023),
                        rpcUsers = listOf(user)),
                startNode(
                        providedName = CordaX500Name("WorldWideImporters", "Shanghai", "CN"),
                        customOverrides = mapOf("rpcSettings.address" to "localhost:10031",
                                "rpcSettings.adminAddress" to "localhost:10032",
                                "sshd.port" to 10033),
                        rpcUsers = listOf(user)),
                startNode(
                        providedName = CordaX500Name("NorthwindTraders", "Copenhagen", "DK"),
                        customOverrides = mapOf("rpcSettings.address" to "localhost:10041",
                                "rpcSettings.adminAddress" to "localhost:10042",
                                "sshd.port" to 10043),
                        rpcUsers = listOf(user)))

        val (nodeA, nodeB, nodeC) = nodeFutures.map { it.getOrThrow() }


    }
}
