package net.corda.workbench.transactionBuilder.agent

import io.javalin.Javalin
import net.corda.workbench.commons.event.FileEventStore
import net.corda.workbench.commons.registry.Registry
import net.corda.workbench.transactionBuilder.agent.api.*
import net.corda.workbench.transactionBuilder.events.Repo

class JavalinAgent(private val port: Int) {

    fun init(): Javalin {

        val app = Javalin.create().apply {
            port(port)
            exception(Exception::class.java) { e, ctx ->
                // build the standard error response
                ctx.status(500)
                val payload = mapOf(
                        "message" to e.message,
                        "stackTrace" to e.stackTrace.joinToString("\n")
                )
                ctx.json(payload)
            }

            error(404) { ctx ->
                val payload = mapOf("message" to "page not found")
                ctx.json(payload)
            }

            //enableStaticFiles("/www", Location.CLASSPATH)
        }


        val dataDir = System.getProperty("user.home") + "/.corda-transaction-builder/events"
        val registry = Registry()
        val es = FileEventStore().load(dataDir)
        registry.store(es)
        registry.store(Repo(es))
        registry.store(app)

        PingApi(registry).register()
        QueryApi(registry).register()
        FlowApi(registry).register()
        app.start()
        println("Agent ready on port $port :)")
        return app
    }
}




