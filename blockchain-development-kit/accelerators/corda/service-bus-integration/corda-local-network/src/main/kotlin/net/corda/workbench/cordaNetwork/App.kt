package net.corda.workbench.cordaNetwork

import net.corda.workbench.commons.event.FileEventStore
import net.corda.workbench.commons.processManager.ProcessManager
import net.corda.workbench.commons.registry.Registry
import net.corda.workbench.cordaNetwork.web.WebController2
import org.http4k.server.Jetty
import org.http4k.server.asServer

fun main(args: Array<String>) {

    val registry = Registry()
    val dataDir = System.getProperty("user.home") + "/.corda-local-network/events"
    val es = FileEventStore().load(dataDir)
    registry.store(es)
    registry.store(ProcessManager())


    val server =  WebController2(registry).asServer(Jetty(1115)).start()
    println ("$server started!")



    Javalin(1114).init(registry)
}