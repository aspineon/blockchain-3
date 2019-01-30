
package net.corda.workbench.simpleMarketplace

import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import net.corda.testing.internal.chooseIdentityAndCert
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkNotarySpec
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.StartedMockNode
import net.corda.workbench.simpleMarketplace.contract.MarketPlaceContract
import net.corda.workbench.simpleMarketplace.flow.MarketFlow
import net.corda.workbench.simpleMarketplace.state.AcceptedState
import net.corda.workbench.simpleMarketplace.state.AvailableItem
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.assertEquals


/**
 * Practical exercise instructions Flows part 1.
 * Uncomment the unit tests and use the hints + unit test body to complete the FLows such that the unit tests pass.
 */
class CreateFlowTests {

    companion object {

        lateinit var mockNetwork: MockNetwork
        lateinit var a: StartedMockNode
        lateinit var b: StartedMockNode
        lateinit var c: StartedMockNode
        lateinit var d: StartedMockNode
        private val allParties = ArrayList<Party>()

        @BeforeClass
        @JvmStatic
        fun setup() {
            mockNetwork = MockNetwork(listOf("net.corda.workbench.simpleMarketplace"),
                    notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary", "London", "GB"))))
            a = mockNetwork.createNode(MockNodeParameters())
            b = mockNetwork.createNode(MockNodeParameters())
            c = mockNetwork.createNode(MockNodeParameters())
            d = mockNetwork.createNode(MockNodeParameters())


            allParties.add(party(a))
            allParties.add(party(b))
            allParties.add(party(c))
            allParties.add(party(d))

            val startedNodes = arrayListOf(a, b, c, d)
            // For real nodes this happens automatically, but we have to manually register the flow for tests
            startedNodes.forEach { it.registerInitiatedFlow(MarketFlow.Acceptor::class.java) }



            mockNetwork.runNetwork()
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            //mockNetwork.
            mockNetwork.stopNodes()
        }

        fun party(node: StartedMockNode): Party {
            return node.info.chooseIdentityAndCert().party
        }
    }

    /**
     * The basic happy path. The flow is run and is completed, signed by the seller
     * and committed to the seller's and buyer's vault.
     */
    @Test
    fun flowHappyPath() {

        val owner = party(a)
        val buyer = party(b)
        val supplyChainOwner = party(c)
        val supplyChainObserver = party(d)

        val av = AvailableItem(description = "item01",price = 100.00)

//        val item = AcceptedState(owner = owner, buyer = buyer,offeredPrice = 100.00)
        val flow = MarketFlow(av,100.00,buyer)
        val future = a.startFlow(flow)
        mockNetwork.runNetwork()

        // Return the unsigned(!) SignedTransaction object from the CreateFlow.
        val ptx: SignedTransaction = future.getOrThrow()
        println(ptx.tx)

        // Check the transaction is well formed...
        assert(ptx.tx.inputs.isEmpty())
        assert(ptx.tx.outputs.single().data is AcceptedState)
        val command = ptx.tx.commands.single()
        assert(command.value is MarketPlaceContract.Commands.CreateTransfert)



        listOf(a).map {
            it.services.validatedTransactions.getTransaction(ptx.id)
        }.forEach {
            val txHash = (it as SignedTransaction).id
            println("$txHash == ${ptx.id}")
            assertEquals(ptx.id, txHash)
        }
    }
}

