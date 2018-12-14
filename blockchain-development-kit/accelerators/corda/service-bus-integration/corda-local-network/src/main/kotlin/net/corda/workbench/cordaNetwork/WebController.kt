package net.corda.workbench.cordaNetwork


import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.util.options.MutableDataSet
import com.vladsch.flexmark.parser.Parser
import io.javalin.ApiBuilder
import io.javalin.Context

import net.corda.workbench.commons.event.EventStore
import net.corda.workbench.commons.registry.Registry
import net.corda.workbench.transactionBuilder.readFileAsText

import org.http4k.core.*
import org.http4k.core.body.formAsMap
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.slf4j.Logger
import java.io.FileInputStream

class WebController2(private val registry: Registry) : HttpHandler {

//    private val logger: Logger = loggerFor<WebController>()
//    private val appRepo = AppRepo(registry.retrieve(EventStore::class.java))
//    val networkManager: String = "http://corda-local-network:1114"
//    val loader: CordaAppLoader = CordaAppLoader().scan()


    private val routes = routes(
            "/" bind Method.GET to {
                Response(Status.PERMANENT_REDIRECT).header("Location", "/web/index")
            },
            "/web" bind routes(
                    "/index" bind Method.GET to {
//                        val apps = appRepo.allApps()
                        val page = renderTemplate("index.md",
                                mapOf("apps" to "apps"))
                        Response(Status.OK)
                                .body(page)
                                .header("Content-Type", "text/html; charset=utf-8")

                    }
//                    "{network}/{app}" bind Method.GET to { req ->
//                        val network = req.path("network")!!
//                        val appName = req.path("app")!!
//                        val app = appRepo.findApp(network, appName)
//                        if (app != null) {
//                            val flows = FlowMetaDataExtractor("net.corda.workbench.refrigeratedTransportation").allFlows()
//
//                            val page = renderTemplate("appdetails.md",
//                                    mapOf("app" to app, "flows" to flows))
//                            html(page)
//
//                        } else {
//                            notFound("App $appName doesn't exist")
//                        }
//                    },
//                    "{network}/{app}/{flow}/{metadata}" bind Method.GET to { req ->
//                        val network = req.path("network")!!
//                        val appName = req.path("app")!!
//                        val app = appRepo.findApp(network, appName)
//
//                        if (app != null) {
//                            val flowName = req.path("flow")!!
//
//                            val params = FlowMetaDataExtractor("net.corda.workbench.refrigeratedTransportation")
//                                    .primaryConstructorMetaData(flowName)
//
//                            val page = renderTemplate("metadata.md",
//                                    mapOf("app" to app, "params" to params.entries))
//                            html(page)
//                        } else {
//                            notFound("App $appName doesn't exist")
//                        }
//
//                    },
//                    "{network}/{app}/{flow}/run" bind Method.POST to { req ->
//
//
//                        val flowName = req.path("flow")!!
//
//                        println(flowName)
//
//                        val shortFlowName = flowName.split(".").last()
//                        println("short flow is ${shortFlowName}")
//
//
//                        val nodeConfig = lookupNodeConfig(req)
//                        val helper = RPCHelper("corda-local-network:${nodeConfig.port}")
//                        helper.connect()
//                        val client = helper.cordaRPCOps()!!
//                        val resolver = RpcPartyResolver(helper)
//
//                        println("resolver: $resolver")
//
//                        // todo - fix to pass multiple packages
//                        val reporter = StringReporter()
//                        val runner = FlowRunner("net.corda.workbench.refrigeratedTransportation",
//                                resolver,
//                                LiveRpcCaller(client),
//                                reporter)
//
//                        println("runner: $runner")
//
//
//                        val data = req.formAsMap()
//
//
//                        val d1 = HashMap<String, Any>()
//                        for (x in data.entries) {
//                            println(x.key + " - " + x.value.size)
//
//                            val value = x.value[0]
//
//                            if (StringUtil.isNumeric(value)) {
//                                d1[x.key] = value!!.toInt()
//                            } else {
//                                d1[x.key] = value!!
//                            }
//                        }
//
//
//                        for (x in d1.entries) {
//                            println(x.key + " - " + x.value)
//                        }
//
//
//                        val result = runner.run<Any>(shortFlowName, d1)
//
//                        text(reporter.result)
//
//
//                    }

            )

    )

    override fun invoke(p1: Request) = routes(p1)

    private fun html(page: String): Response {
        return Response(Status.OK)
                .body(page)
                .header("Content-Type", "text/html; charset=utf-8")

    }

    private fun text(page: String): Response {
        return Response(Status.OK)
                .body(page)
                .header("Content-Type", "text/plain; charset=utf-8")

    }

    private fun notFound(message: String): Response {
        return Response(Status.NOT_FOUND)
                .body(message)
                .header("Content-Type", "text/plain; charset=utf-8")

    }


    fun renderTemplate(path: String, params: Map<String, Any?> = emptyMap()): String {
        val html = renderMustache(path, params)

        // merge with layout.html.html
        val layout = FileInputStream("src/main/resources/www/layout.html").bufferedReader().use { it.readText() }
        return layout.replace("<!--BODYTEXT-->", html, false)
    }

    private fun renderMustache(path: String, params: Map<String, Any?>): String {
        try {
            // mustache processing
            val content = readFileAsText("src/main/resources/www/$path", params)

            // markdown processing
            val options = MutableDataSet()
            val parser = Parser.builder(options).build()
            val renderer = HtmlRenderer.builder(options).build()
            val document = parser.parse(content)
            return renderer.render(document)
        } catch (ex: Exception) {
            return "<pre>" + ex.message!! + "</pre>"
        }
    }


}