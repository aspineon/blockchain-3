package net.corda.workbench.simpleMarketplace

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
    driver(DriverParameters(waitForAllNodesToFinish = true)) {
        val nodeFutures = listOf(
                startNode(
                        providedName = CordaX500Name("Owner", "London", "GB"),
                        customOverrides = mapOf("rpcSettings.address" to "localhost:10008", "rpcSettings.adminAddress" to "localhost:10048", "webAddress" to "localhost:10009"),
                        rpcUsers = listOf(user)),
                startNode(
                        providedName = CordaX500Name("Buyer", "New York", "US"),
                        customOverrides = mapOf("rpcSettings.address" to "localhost:10011", "rpcSettings.adminAddress" to "localhost:10051", "webAddress" to "localhost:10012"),
                        rpcUsers = listOf(user)))


        val (nodeA, nodeB) = nodeFutures.map { it.getOrThrow() }


    }
}
