package net.corda.workbench.refrigeratedTransportation

import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.driver.DriverParameters
import net.corda.testing.driver.driver
import net.corda.testing.node.User

/**
 * This file is exclusively for being able to run your nodes through an IDE (as opposed to using deployNodes)
 * Do not use in a production environment.
 *
 * To debug your CorDapp:
 *
 * 1. Run the "Run Template CorDapp" run configuration.
 * 2. Wait for all the nodes to start.
 * 3. Note the debug ports for each node, which should be output to the console. The "Debug CorDapp" configuration runs
 *    with port 5007, which should be "PartyA". In any case, double-check the console output to be sure.
 * 4. Set your breakpoints in your CorDapp code.
 * 5. Run the "Debug CorDapp" remote debug run configuration.
 */
fun main(args: Array<String>) {

    val user = User("user1", "test", permissions = setOf("ALL"))
    driver(DriverParameters(waitForAllNodesToFinish = true, startNodesInProcess = true)) {
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
                        rpcUsers = listOf(user)),
                startNode(
                        providedName = CordaX500Name("WoodgroveBank", "London", "GB"),
                        customOverrides = mapOf("rpcSettings.address" to "localhost:10051",
                                "rpcSettings.adminAddress" to "localhost:10052",
                                "sshd.port" to 10053),
                        rpcUsers = listOf(user)),
                startNode(
                        providedName = CordaX500Name("Device01", "London", "GB"),
                        customOverrides = mapOf("rpcSettings.address" to "localhost:10061",
                                "rpcSettings.adminAddress" to "localhost:10062",
                                "sshd.port" to 10063),
                        rpcUsers = listOf(user)),
                startNode(
                        providedName = CordaX500Name("Device02", "Shanghai", "CN"),
                        customOverrides = mapOf("rpcSettings.address" to "localhost:10071",
                                "rpcSettings.adminAddress" to "localhost:10072",
                                "sshd.port" to 10073),
                        rpcUsers = listOf(user)),
                startNode(
                        providedName = CordaX500Name("TasmanianTraders", "Hobart", "AU"),
                        customOverrides = mapOf("rpcSettings.address" to "localhost:10081",
                                "rpcSettings.adminAddress" to "localhost:10082",
                                "sshd.port" to 10083),
                        rpcUsers = listOf(user)))


        val nodes = nodeFutures.map { it.getOrThrow() }
        println(nodes)


    }
}
