package net.corda.workbench.cordaNetwork.web

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.containsSubstring
import net.corda.workbench.commons.event.FileEventStore
import net.corda.workbench.commons.registry.Registry
import net.corda.workbench.transactionBuilder.events.EventFactory
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

import org.http4k.hamkrest.hasBody
import org.http4k.hamkrest.hasContentType
import org.http4k.hamkrest.hasHeader
import org.http4k.hamkrest.hasStatus

@RunWith(JUnitPlatform::class)
object ControllerSpec : Spek({

    val baseUrl = "http://corda-local-network:1115/"

    val es = FileEventStore()
    val registry = Registry().store(es)
    val controller = WebController2(registry)

    describe("The Web Controller") {

        beforeEachTest {
            es.truncate()
        }

        it("should redirect to home page") {
            val response = controller(Request(Method.GET, "/"))

            assertThat(response, hasStatus(Status.PERMANENT_REDIRECT))
            assertThat(response, hasHeader("Location", "/web/home"))
        }

        it("should render home page") {
            val response = controller(Request(Method.GET, "/web/home"))

            assertThat(response, hasStatus(Status.OK))
            // todo - would be nice to add some JQuery style matchers driven by https://jsoup.org/
            // https://github.com/ianmorgan/canfactory-html
            assertThat(response, hasBody(containsSubstring("Corda Local Network")))
        }

        it("should render list of networks on home page") {
            es.storeEvent(EventFactory.NETWORK_CREATED("NetworkA"))
            val response = controller(Request(Method.GET, "/web/home"))

            assertThat(response, hasStatus(Status.OK))
            assertThat(response, hasBody(containsSubstring("Available Networks")))
            assertThat(response, hasBody(containsSubstring("NetworkA")))
        }

        it("should render list of nodes for network") {
            es.storeEvent(EventFactory.NETWORK_CREATED("net1"))
            es.storeEvent(EventFactory.NODES_CREATED("net1", listOf("O=Alice,L=London,C=GB","O=Bob,L=New York,C=US")))
            val response = controller(Request(Method.GET, "/web/networks/net1"))

            assertThat(response, hasStatus(Status.OK))
            assertThat(response, hasBody(containsSubstring("Network net1")))
            assertThat(response, hasBody(containsSubstring("Alice")))
            assertThat(response, hasBody(containsSubstring("Bob")))

        }


    }
})


