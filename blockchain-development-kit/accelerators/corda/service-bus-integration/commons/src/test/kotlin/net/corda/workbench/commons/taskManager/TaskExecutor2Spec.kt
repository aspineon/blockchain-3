import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import net.corda.workbench.commons.taskManager.TaskExecutor
import net.corda.workbench.commons.taskManager.TaskExecutor2
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
object TaskExecutor2Spec : Spek({

    describe("Running tasks ") {

        it("should run a simple task ") {

            val t = TestTask()
            val sink = TestMessageSink()
            val executor = TaskExecutor2 { sink.sink(it) }
            executor.exec(t)

            assert.that(sink.messages(),
                    equalTo(listOf("Starting TestTask", "executing...", "Completed TestTask")))

        }

//        it("should run a failing task ") {
//
//            val t = FailingTask()
//            val sink = TestMessageSink()
//            val executor = TaskExecutor({ sink.sink(it) })
//            executor.exec(t)
//
//            assert.that(sink.messages(),
//                    equalTo(listOf("Starting FailingTask", "Failed FailingTask", "Exception is: forced an error")))
//
//        }
    }
})
