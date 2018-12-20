package net.corda.workbench.commons.taskManager


import java.util.*

/**
 * A simple execution context to control where messages and logging are sent to
 */
class ExecutionContext(messageSink: (String) -> Unit = { consoleMessageSink(it) },
                       outputSink: (String) -> Unit = { consoleMessageSink(it) },
                       errorSink: (String) -> Unit = { consoleMessageSink(it) },
                       id: UUID = UUID.randomUUID()
) {

    val id: UUID = id

    /**
     * Standard output stream. Tasks should redirect any application output to this sink
     */
    //val outputStream: OutputStream = System.out

    val outputSink: (String) -> Unit = outputSink


    /**
     * Standard error stream. Tasks should redirect any application errors to this sink
     */
    val errorSink: (String) -> Unit = errorSink


    /**
     * Dedicated stream for the task's internal reporting. Should
     * be limited to basic status and progress messages
     */
    val messageSink: (String) -> Unit = messageSink


    @Deprecated(message = "use messageSink instead")
    val messageStream: (String) -> Unit = messageSink

    /**
     * A default sink (consumer) of messages that just prints to
     * the console
     */
    companion object {
        fun consoleMessageSink(m: String) {
            println(m)
        }
    }
}

/**
 * Executes a single task with logging
 */
class TaskExecutor(private val taskLogMessageSink: (TaskLogMessage) -> Unit) {

    val executorId: UUID = UUID.randomUUID()

    fun exec(t: Task) {

        // wire up an execution context linked to this taskLogMessageSink, so that
        // executionContext.messageStream is sent via TaskLogMessage
        val sink = MessageSink(executorId, t, taskLogMessageSink)
        val executionContext = ExecutionContext({ message -> sink.doit(message) })

        //sink.doit("Starting ${t::class.java.simpleName}")

        taskLogMessageSink.invoke(TaskLogMessage(executorId, "Starting ${t::class.java.simpleName}", t.taskID))
        try {
            t.exec(executionContext)
            taskLogMessageSink.invoke(TaskLogMessage(executorId, "Completed ${t::class.java.simpleName}", t.taskID))

        } catch (ex: Exception) {
            taskLogMessageSink.invoke(TaskLogMessage(executorId, "Failed ${t::class.java.simpleName}", t.taskID))
            taskLogMessageSink.invoke(TaskLogMessage(executorId, "Exception is: ${ex.message}", t.taskID))
        }
    }

    class MessageSink(val executorId: UUID, val t: Task, private val taskLogMessageSink: (TaskLogMessage) -> Unit) {
        fun doit(message: String) {
            val taskLogMessage = TaskLogMessage(executorId, message, t.taskID)
            taskLogMessageSink.invoke(taskLogMessage)
        }
    }
}


/**
 * Executes a single task with logging
 */
class TaskExecutor2(private val taskLogMessageSink: (TaskLogMessage) -> Unit) {

    val executorId: UUID = UUID.randomUUID()

    fun exec(t: Task) {

        // wire up an execution context linked to this taskLogMessageSink, so that
        // executionContext.messageStream is sent via TaskLogMessage
        val sink = MessageSink(executorId, t, taskLogMessageSink)
        val executionContext = ExecutionContext({ sink.doit(it) })

        taskLogMessageSink.invoke(TaskLogMessage(executorId, "Starting ${t::class.java.simpleName}", t.taskID))
        try {
            t.exec(executionContext)
            taskLogMessageSink.invoke(TaskLogMessage(executorId, "Completed ${t::class.java.simpleName}", t.taskID))

        } catch (ex: Exception) {
            taskLogMessageSink.invoke(TaskLogMessage(executorId, "Failed ${t::class.java.simpleName}", t.taskID))
            taskLogMessageSink.invoke(TaskLogMessage(executorId, "Exception is: ${ex.message}", t.taskID))
        }
    }

    class MessageSink(val executorId: UUID, val t: Task, private val taskLogMessageSink: (TaskLogMessage) -> Unit) {
        fun doit(message: String) {
            val taskLogMessage = TaskLogMessage(executorId, message, t.taskID)
            taskLogMessageSink.invoke(taskLogMessage)
        }
    }
}


/**
 * Execute a list of tasks, waiting until the last one completes
 */
class BlockingTasksExecutor(private val taskLogMessageSink: (TaskLogMessage) -> Unit) {

    fun exec(tasks: List<Task>) {
        tasks.forEach { TaskExecutor(taskLogMessageSink).exec(it) }
    }

    fun exec(task: Task) {
        exec(listOf(task))
    }
}
