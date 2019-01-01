package net.corda.workbench.cordaNetwork.tasks

import net.corda.workbench.commons.taskManager.DataTask
import net.corda.workbench.commons.taskManager.ExecutionContext
import net.corda.workbench.commons.taskManager.TaskContext
import java.io.File

/**
 * A Data task which makes calls to all nodes on the network to determine their status
 */
class NodesStatusTask(val ctx: TaskContext) : DataTask<List<Map<String, Any>>> {

    override fun exec(executionContext: ExecutionContext): List<Map<String, Any>> {
        val results = ArrayList<Map<String, Any>>()
        for (node in nodesIter()) {
            val nodeStatus = HashMap(NodeStatusTask(ctx, node.name).exec(executionContext))
            nodeStatus["node"] = node.name
            results.add(nodeStatus)
        }

        return results;
    }

    fun nodesIter(): Sequence<File> {
        return File(ctx.workingDir).walk()
                .maxDepth(1)
                .filter { it.isDirectory && it.name.endsWith("_node") }
    }
}