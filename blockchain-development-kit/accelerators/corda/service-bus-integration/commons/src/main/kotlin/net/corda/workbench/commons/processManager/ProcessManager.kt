package net.corda.workbench.commons.processManager


import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap
import kotlin.concurrent.thread

/**
 * Keeps track of running processes
 */


class ProcessManager constructor(
        private val outputSink: (String) -> Unit = { msg -> ProcessMonitor.consoleMessageSink(msg) },
        private val errorSink: (String) -> Unit = { msg -> ProcessMonitor.consoleMessageSink(msg) },
        private val processCompletedSink: (ProcessManager.ManagedProcess, Int) -> Unit = { _, _ -> }

) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ProcessManager::class.java)
    }

    private val sleepTime = 10
    private var monitoring = false
    private val processList = ArrayList<ManagedProcess>()
    private val processMonitors = HashMap<UUID, ProcessMonitor>()


    fun register(process: Process, id: UUID = UUID.randomUUID(), label: String = "") {
        val mp = ManagedProcess(process, id, label)
        processList.add(mp)
        processMonitors[id] = ProcessMonitor(mp, outputSink, errorSink, processCompletedSink)
    }

    fun findByProcess(process: Process): ProcessInfo? {
        val mp = processList.singleOrNull() { it.process == process }
        return if (mp == null) null else mapToProcessInfo(mp)
    }

    fun findById(id: UUID): ProcessInfo? {
        val mp = processList.singleOrNull { it.id == id }
        return if (mp == null) null else mapToProcessInfo(mp)
    }

    fun findByLabel(label: String): ProcessInfo? {
        val mp = processList.singleOrNull { it.label == label }
        return if (mp == null) null else mapToProcessInfo(mp)
    }

    fun allProcesses(): List<ProcessInfo> {
        return processList.map { mapToProcessInfo(it) }
    }


    private fun mapToProcessInfo(mp: ManagedProcess): ProcessInfo {
        val monitor = processMonitors[mp.id]!!
        return ProcessInfo(process = mp.process, id = mp.id, label = mp.label, monitor = monitor)
    }

    fun monitor() {
        if (!monitoring) {
            println("Process monitor started")
            monitoring = true
            thread(isDaemon = true) {
                while (monitoring) {
                    println("Process monitor checking ${processList.size} processes...")
                    processList.forEach {
                        println("  process: ${it.label}, isAlive? ${it.process.isAlive}")
                    }
                    println("Process monitor sleeping for $sleepTime...")
                    Thread.sleep(sleepTime * 1000L)
                }
                println("Process monitor stopped")
            }
        }
    }



    /**
     * Kill everything - rather brutal, not for everyday use.
     */
    fun killAll() {
        for (p in processList) {
            println("Forcibly killing $p")
            brutalKill(p.process)
        }
        processList.clear()

    }


    private fun brutalKill(process: Process) {
        val pid = getPidOfProcess(process)
        if (pid != -1L) {
            pkill(pid)
        }
        try {
            process.destroyForcibly()
        } catch (ignored: Exception) {
        }
    }


    @Synchronized
    fun getPidOfProcess(p: Process): Long {
        var pid: Long = -1

        try {
            if (p.javaClass.name == "java.lang.UNIXProcess") {
                val f = p.javaClass.getDeclaredField("pid")
                f.isAccessible = true
                pid = f.getLong(p)
                f.isAccessible = false
            }
        } catch (e: Exception) {
            pid = -1
        }

        return pid
    }

    private fun pkill(pid: Long) {
        logger.debug("using pkill on PID $pid")
        val pb = ProcessBuilder(listOf("pkill", "-P", pid.toString()))
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()

        pb.waitFor(5, TimeUnit.SECONDS)
        logger.debug("stdout: " + pb.inputStream.bufferedReader().use { it.readText() })
        logger.debug("err:" + pb.errorStream.bufferedReader().use { it.readText() })

    }

    data class ManagedProcess(val process: Process, val id: UUID = UUID.randomUUID(), val label: String = "")

    data class ProcessInfo(val process: Process, val id: UUID, val label: String, val monitor: ProcessMonitor)


}


class ProcessMonitor constructor(private val managedProcess: ProcessManager.ManagedProcess,
                                 private val outputSink: (String) -> Unit = { msg -> consoleMessageSink(msg) },
                                 private val errorSink: (String) -> Unit = { msg -> consoleMessageSink(msg) },
                                 private val processCompletedSink: (ProcessManager.ManagedProcess, Int) -> Unit = { _, _ -> }

) {
    var exitCode: Int? = null

    init {
        val outputMonitor = monitorOutput()
        val errorMonitor = monitorError()
        monitorRunning(outputMonitor, errorMonitor)
    }

    private fun monitorOutput(): Thread {
        return thread() {
            val br = BufferedReader(InputStreamReader(managedProcess.process.inputStream))
            var line: String? = br.readLine()
            do {
                while (line != null) {

                    outputSink.invoke(line)
                    line = br.readLine()
                }
                Thread.sleep(1000)
            } while (true)
        }
    }

    private fun monitorError(): Thread {
        return thread() {
            val br = BufferedReader(InputStreamReader(managedProcess.process.errorStream))
            var line: String? = br.readLine()
            do {
                while (line != null) {
                    errorSink.invoke(line)
                    line = br.readLine()
                }
                Thread.sleep(1000)
            } while (true)
        }
    }

    private fun monitorRunning(outputMonitor: Thread, errorMonitor: Thread) {
        thread() {
            do {
                if (managedProcess.process.isAlive) {
                    Thread.sleep(1000)
                } else {
                    exitCode = managedProcess.process.exitValue()
                    outputSink("${managedProcess.id} has completed with exit code $exitCode")
                    processCompletedSink.invoke(managedProcess, exitCode as Int)


                    //  fixme !
                    outputMonitor.stop()
                    errorMonitor.stop()

                    break;
                }

            } while (true)
        }
    }

    fun isRunning(): Boolean {
        return exitCode == null
    }

    fun exitCode(): Int {
        return exitCode!!
    }

    /**
     * A default sink (consumer) of messages that just prints to the console
     */
    companion object {
        fun consoleMessageSink(m: String) {
            println(m)
        }
    }
}