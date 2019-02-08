package net.corda.workbench.transactionBuilder.events

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import net.corda.workbench.commons.event.EventStore
import net.corda.workbench.commons.event.FileEventStore
import net.corda.workbench.transactionBuilder.CordaAppConfig
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.util.*

@RunWith(JUnitPlatform::class)
object RepoSpec : Spek({

    lateinit var es: EventStore
    lateinit var repo: Repo

    describe("A simple event sourced based repo") {

        beforeEachTest {
            es = FileEventStore()
            repo = Repo(es)
        }

        it("should return joined networks") {
            es.storeEvent(EventFactory.NETWORK_JOINED("networkA", listOf("Alice", "Bob")))
            es.storeEvent(EventFactory.NETWORK_JOINED("networkB", listOf("Charlie", "Dan")))

            val networks = repo.joinedNetworks().sorted()
            assertThat(networks.size, equalTo(2))
            assertThat(networks, equalTo(listOf("networkA", "networkB")))
        }


        it("should return nodes for a network") {
            es.storeEvent(EventFactory.NETWORK_JOINED("networkA", listOf("Alice", "Bob")))
            es.storeEvent(EventFactory.NETWORK_JOINED("networkB", listOf("Charlie", "Dan")))
            es.storeEvent(EventFactory.NETWORK_JOINED("networkA", listOf("Org1", "Org2")))


            assertThat(repo.nodes("networkA"), equalTo(listOf("Org1", "Org2")))
            assertThat(repo.nodes("networkB"), equalTo(listOf("Charlie", "Dan")))
        }


        it("should load allCordaAppConfigs") {
            val app1Id = UUID.randomUUID()
            val app2Id = UUID.randomUUID()
            es.storeEvent(EventFactory.CORDA_APP_DEPLOYED("networkA", "app1", app1Id, listOf("net.corda")))
            es.storeEvent(EventFactory.CORDA_APP_DEPLOYED("networkB", "app1", app1Id, listOf("net.corda")))
            es.storeEvent(EventFactory.CORDA_APP_DEPLOYED("networkA", "app2", app2Id, listOf("net.corda")))
            es.storeEvent(EventFactory.CORDA_APP_DEPLOYED("networkA", "app2", app2Id, listOf("com.r3", "net.corda")))

            assertThat(repo.allCordaAppConfigs("networkA"), equalTo(
                    listOf(CordaAppConfig(app1Id, "app1", listOf("net.corda")),
                            CordaAppConfig(app2Id, "app2", listOf("com.r3", "net.corda")))))

            assertThat(repo.allCordaAppConfigs("networkB"), equalTo(
                    listOf(CordaAppConfig(app1Id, "app1", listOf("net.corda")))))

            assertThat(repo.allCordaAppConfigs("networkC"), equalTo(emptyList()))

        }

        it("should load cordaAppConfig") {
            val app1Id = UUID.randomUUID()
            val app2Id = UUID.randomUUID()
            es.storeEvent(EventFactory.CORDA_APP_DEPLOYED("networkA", "app1", app1Id, listOf("net.corda")))
            es.storeEvent(EventFactory.CORDA_APP_DEPLOYED("networkB", "app1", app1Id, listOf("net.corda")))
            es.storeEvent(EventFactory.CORDA_APP_DEPLOYED("networkA", "app2", app2Id, listOf("net.corda")))
            es.storeEvent(EventFactory.CORDA_APP_DEPLOYED("networkA", "app2", app2Id, listOf("com.r3", "net.corda")))

            assertThat(repo.cordaAppConfig("networkA", "app1"),
                    equalTo(CordaAppConfig(app1Id, "app1", listOf("net.corda"))))
            assertThat(repo.cordaAppConfig("networkA", "app2"),
                    equalTo(CordaAppConfig(app2Id, "app2", listOf("com.r3", "net.corda"))))
            assertThat(repo.cordaAppConfig("networkB", "app1"),
                    equalTo(CordaAppConfig(app1Id, "app1", listOf("net.corda"))))
            assertThat(repo.cordaAppConfig("networkB", "app2") , equalTo(null as CordaAppConfig?) )

        }

    }

})