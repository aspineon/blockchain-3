package net.corda.workbench.cordaNetwork.web


import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.util.options.MutableDataSet
import com.vladsch.flexmark.parser.Parser
import net.corda.workbench.commons.event.EventStore
import net.corda.workbench.commons.event.Filter

import net.corda.workbench.commons.registry.Registry
import net.corda.workbench.commons.taskManager.*
import net.corda.workbench.cordaNetwork.events.Repo
import net.corda.workbench.cordaNetwork.tasks.*
import net.corda.workbench.transactionBuilder.readFileAsText

import org.http4k.core.*
import org.http4k.core.body.formAsMap
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileInputStream

class WebController2(private val registry: Registry) : HttpHandler {
    val repo = Repo(registry.retrieve(EventStore::class.java))

    // todo - should be injected in
    private val taskRepos = HashMap<String, TaskRepo>()


    private val routes = routes(
            "/" bind Method.GET to {
                Response(Status.PERMANENT_REDIRECT).header("Location", "/web/home")
            },
            "/web" bind routes(
                    "/home" bind Method.GET to {
                        val networks = repo.networks()
                        val page = renderTemplate("home.md",
                                mapOf("networks" to networks))
                        html(page)

                    },


                    "/networks/create" bind Method.GET to { req ->
                        val page = renderTemplate("createNetworkForm.html", emptyMap())
                        html(page)

                    },

                    "/networks/create" bind Method.POST to { req ->
                        val createRequest = unpackCreateNetworkForm(req)
                        val context = RealContext(createRequest.name)
                        val executor = buildExecutor(context)
                        val scopedRegistry = registry.overide(context)

                        executor.exec(CreateNetworkTask(scopedRegistry))
                        executor.exec(CreateNodesTask(scopedRegistry, createRequest.organisations))

                        val page = renderTemplate("networkCreated.md",
                                mapOf("networkName" to createRequest.name))
                        html(page)

                    },

                    "/networks/{network}" bind Method.GET to { req ->
                        val network = req.path("network")!!
                        val nodes = repo.nodes(network)
                        val page = renderTemplate("network.md",
                                mapOf("nodes" to nodes, "name" to network))
                        html(page)

                    },

                    "/networks/{network}/start" bind Method.POST to { req ->
                        val network = req.path("network")!!
                        val context = RealContext(network)
                        val executor = buildExecutor(context)
                        val scopedRegistry = registry.overide(context)

                        if (!isNetworkRunning(network)) {
                            val tasks = listOf(StopCordaNodesTask(scopedRegistry), StartCordaNodesTask(scopedRegistry))
                            executor.exec(tasks)

                            val page = renderTemplate("networkStarted.md",
                                    mapOf("networkName" to network))
                            html(page)

                        } else {
                            throw RuntimeException("network $network is already running")
                        }


                    },


                    "/networks/{network}/status" bind Method.GET to { req ->
                        val network = req.path("network")!!
                        val context = RealContext(network)
                        val messageSink = buildMessageSink(context)
                        val executor = TaskExecutor(messageSink)
                        val scopedRegistry = registry.overide(context)

                        val status = NodesStatusTask(context).exec()

                        val nodes = repo.nodes(network)
                        val page = renderTemplate("status.md",
                                mapOf("status" to status, "name" to network))
                        html(page)
                    },


                    "/style.css" bind Method.GET to {
                        val css = FileInputStream("src/main/resources/www/style.css").bufferedReader().use { it.readText() }
                        css(css)
                    },

                    "/ajax/test" bind Method.GET to {
                        text("time is ${System.currentTimeMillis()}")

                    }

            ),
            "/ajax" bind routes(
                    "/networks/{network}/nodes/{node}/status" bind Method.GET to { req ->
                        val network = req.path("network")!!
                        val node = req.path("node")!!
                        val context = RealContext(network)
                        val messageSink = buildMessageSink(context)
//                        val executor = TaskExecutor(messageSink)
//                        val scopedRegistry = registry.overide(context)
                        val status = NodeStatusTask(context,node).exec()
                       json(status)
                    }

            )

    )

    override fun invoke(p1: Request) = routes(p1)

    private fun html(page: String): Response {
        return Response(Status.OK)
                .body(page)
                .header("Content-Type", "text/html; charset=utf-8")

    }

    private fun css(page: String): Response {
        return Response(Status.OK)
                .body(page)
                .header("Content-Type", "text/css; charset=utf-8")

    }

    private fun text(page: String): Response {
        return Response(Status.OK)
                .body(page)
                .header("Content-Type", "text/plain; charset=utf-8")

    }

    private fun json(data: Map<String,Any>): Response {
        return Response(Status.OK)
                .body(JSONObject(data).toString(2))
                .header("Content-Type", "application/json; charset=utf-8")

    }


    private fun notFound(message: String): Response {
        return Response(Status.NOT_FOUND)
                .body(message)
                .header("Content-Type", "text/plain; charset=utf-8")

    }

    private fun renderTemplate(path: String, params: Map<String, Any?> = emptyMap()): String {
        val html = renderMustache(path, params)

        // merge with layout.html.html
        val layout = FileInputStream("src/main/resources/www/layout.html").bufferedReader().use { it.readText() }
        val result = layout.replace("<!--BODYTEXT-->", html, false)
        //println(result)
        return result
    }


    private fun renderMustache(path: String, params: Map<String, Any?>): String {
        try {
            // mustache processing
            val content = readFileAsText("src/main/resources/www/$path", params)

            // markdown processing
            if (path.endsWith(".md")) {
                val options = MutableDataSet()
                val parser = Parser.builder(options).build()
                val renderer = HtmlRenderer.builder(options).build()
                val document = parser.parse(content)
                return renderer.render(document)
            } else {
                return content
            }
        } catch (ex: Exception) {
            return "<pre>" + ex.message!! + "</pre>"
        }
    }


    private fun buildMessageSink(context: TaskContext): ((TaskLogMessage) -> Unit) {
        val repo = taskRepos.getOrPut(context.networkName) {
            SimpleTaskRepo("${context.workingDir}/tasks")
        }
        return {
            try {
                repo.store(it)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

    }

    private fun buildExecutor(context: TaskContext): BlockingTasksExecutor {
        val messageSink = buildMessageSink(context)
        return BlockingTasksExecutor(messageSink)
    }

    private fun isNetworkRunning(network: String): Boolean {
        // todo - should be moved to a method on the repo
        return registry.retrieve(EventStore::class.java).retrieve(Filter(aggregateId = network))
                .fold(false) { status, event ->
                    when {
                        event.type == "NetworkStarted" -> true
                        event.type == "NetworkStopped" -> false
                        else -> status
                    }
                }
    }


    private fun unpackCreateNetworkForm(req: Request): CreateNetworkRequest {
        try {
            val params = req.formAsMap()

            val name = params["networkName"]!!.single()!!
            val organisations = params["organisations"]!!
                    .single()!!
                    .trim()
                    .split("\n")
                    .map { it.trim() }

            return CreateNetworkRequest(name, organisations + listOf("O=Notary,L=London,C=GB"))
        } catch (ex: Exception) {
            throw RuntimeException("Incorrect params passed to Create Network - '${ex.message}'", ex)
        }
    }


    data class CreateNetworkRequest(val name: String, val organisations: List<String>)


}