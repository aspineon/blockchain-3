package net.corda.workbench.commons.processManager


import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
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
    private val sleepTime = 10
    private var monitoring = false
    private val processes = HashMap<Key, Process>()
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

    fun register(network: String, task: String, process: Process) {
        val key = Key(network, task)
        println("Process $key registered ")
        processes[key] = process
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
                    println("Process monitor checking ${processes.size} processes...")
                    processes.forEach {
                        println("  process: ${it.key}, isAlive? ${it.value.isAlive}")
                    }
                    println("Process monitor sleeping for $sleepTime...")
                    Thread.sleep(sleepTime * 1000L)
                }
                println("Process monitor stopped")
            }
        }
    }

    fun all(): List<Process> {
        return ArrayList(processes.values)
    }

    fun queryForNetwork(network: String): List<ProcessStatus> {
        val results = ArrayList<ProcessStatus>()
        processes.entries.forEach {
            if (it.key.network == network) {
                results.add(ProcessStatus(it.key.processName, it.value.isAlive))
            }
        }
        return results
    }

    fun queryForNodeOnNetwork(network: String, processName: String): Process? {
        return processes[Key(network, processName)]
    }

    fun removeProcess(process: Process) {
        // not the nicest code :(
        processes.entries.forEach {
            if (it.value == process) {
                processes.remove(it.key)
                println("Process ${it.key} removed")
                return
            }
        }
    }

    fun killAll() {
        for (p in processes.entries) {
            println("Forcibly killing ${p.key}")
            p.value.destroyForcibly()
        }
        processes.clear()
    }

    data class Key(val network: String, val processName: String)

    data class ProcessStatus(val name: String, val isAlive: Boolean)

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