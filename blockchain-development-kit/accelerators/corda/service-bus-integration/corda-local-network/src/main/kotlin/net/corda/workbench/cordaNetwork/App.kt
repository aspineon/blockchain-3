package net.corda.workbench.cordaNetwork

import net.corda.workbench.commons.event.FileEventStore
import net.corda.workbench.commons.processManager.ProcessManager
import net.corda.workbench.commons.registry.Registry
import net.corda.workbench.commons.taskManager.TaskExecutor
import net.corda.workbench.cordaNetwork.events.Repo
import net.corda.workbench.cordaNetwork.tasks.RealContext
import net.corda.workbench.cordaNetwork.tasks.StopCordaNodesTask
import net.corda.workbench.cordaNetwork.web.WebController2
import org.http4k.server.Jetty
import org.http4k.server.asServer
import java.net.InetAddress

fun main(args: Array<String>) {

    val registry = Registry()
    val dataDir = System.getProperty("user.home") + "/.corda-local-network/events"
    val es = FileEventStore().load(dataDir)
    val repo = Repo(es)
    val appConfig = AppConfig(publicAddress = InetAddress.getLocalHost())
    registry.store(es).store(ProcessManager()).store(repo)



    val possibleRunningNetwork = repo.runningNodes().map { it.network }.toSet()

    // kill any orphaned process
    for (network in possibleRunningNetwork){
        println ("stopping possible orphaned nodes in $network")
        val context = RealContext(network)
        val executor = TaskExecutor { println(it)}
        val task = StopCordaNodesTask(registry.overide(context))
        executor.exec(task)
    }

    val server =  WebController2(registry).asServer(Jetty(1115)).start()
    println ("$server started!")


    Javalin(1114).init(registry)
}

