package net.corda.workbench.cordaNetwork.tasks

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import net.corda.workbench.commons.event.EventStore
import net.corda.workbench.commons.event.FileEventStore
import net.corda.workbench.commons.processManager.ProcessManager
import net.corda.workbench.commons.registry.Registry
import net.corda.workbench.commons.taskManager.TestContext
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
object StartCordaNodeSpec : Spek({

    val ctx = TestContext("startcordanodespec")
    lateinit var registry:Registry

    //File(ctx.workingDir).deleteRecursively()


    describe("Read a node config") {

        beforeGroup {
            //File(ctx.workingDir).deleteRecursively()
            registry = Registry().store(ctx)
                    .store(FileEventStore())
                    .store(ProcessManager())

            // comment this out to speed up local tests.
            //ConfigBuilderTask(registry, listOf("O=Alice,L=New York,C=US")).exec()
            //NetworkBootstrapperTask(ctx).exec()
        }

        it("should start 'alice_node' ") {
            val task = StartCordaNodeTask(registry, "alice_node")
            val eventStore = registry.retrieve(EventStore::class.java)
            task.exec()

            //assertThat(ProcessManager.all().size, equalTo(1))
            val ev = eventStore.retrieve().last()
            assertThat(ev.payload["node"] as String, equalTo("alice_node"))
            assertThat(ev.payload.containsKey("pid"), equalTo(true))
            assertThat(ev.payload.containsKey("processId"), equalTo(true))


        }


    }
})


//fun main(args : Array<String>) {
//    thread() {
//        var i = 1
//        while (i <= 10) {
//            println("${Thread.currentThread()} is running.")
//            Thread.sleep(1000)
//            i += 1
//
//        }
//        println("${Thread.currentThread()} is done")
//
//    }
//
//    println ("ghdsgjd")
//}

