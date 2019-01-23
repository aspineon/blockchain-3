package net.corda.workbench.basicProvenance

import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import net.corda.testing.internal.chooseIdentityAndCert
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkNotarySpec
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.StartedMockNode
import net.corda.workbench.basicProvenance.contract.ItemContract
import net.corda.workbench.basicProvenance.flow.CreateFlow
import net.corda.workbench.basicProvenance.flow.TransferFlow
import net.corda.workbench.basicProvenance.state.ItemState
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.assertEquals

class TransferFlowTests {

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
            mockNetwork = MockNetwork(listOf("net.corda.workbench.basicProvenance"),
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
            startedNodes.forEach { it.registerInitiatedFlow(CreateFlow.CreateFlowResponder::class.java) }



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
     * and committed to the sellers vault.
     */
    @Test
    fun flowHappyPath() {

        val observer = party(a)
        val otherParty = party(b)
        val creator = party(c)
        // val supplyChainObserver = party(d)


        val item = ItemState(value=100.00,creator = creator,Observer = observer,otherParty = null)
        val flow = CreateFlow(item)
        val future = c.startFlow(flow)
        mockNetwork.runNetwork()

        // Return the unsigned(!) SignedTransaction object from the CreateFlow.
        val ptx: SignedTransaction = future.getOrThrow()
        println(ptx.tx)


        val transferFlow = TransferFlow(item.linearId, otherParty)
        val future2 = c.startFlow(transferFlow)
        mockNetwork.runNetwork()

        val ptx2: SignedTransaction = future2.getOrThrow()

        // Check the transaction is well formed...

        assert(ptx2.tx.outputs.single().data is ItemState)
        val command = ptx2.tx.commands.single()
        assert(command.value is ItemContract.Transfer)


        listOf(c).map {
            it.services.validatedTransactions.getTransaction(ptx.id)
        }.forEach {
            val txHash = (it as SignedTransaction).id
            println("$txHash == ${ptx.id}")
            assertEquals(ptx.id, txHash)
        }
    }
}