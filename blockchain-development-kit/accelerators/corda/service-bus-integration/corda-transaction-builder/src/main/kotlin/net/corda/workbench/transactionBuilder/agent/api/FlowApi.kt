package net.corda.workbench.transactionBuilder.agent.api

import io.javalin.ApiBuilder
import io.javalin.Context
import io.javalin.Javalin
import net.corda.core.utilities.loggerFor

import net.corda.reflections.app.RPCHelper
import net.corda.reflections.reflections.FlowMetaDataExtractor
import net.corda.reflections.reflections.FlowRunner
import net.corda.reflections.reflections.LiveRpcCaller
import net.corda.reflections.resolvers.RpcPartyResolver
import net.corda.workbench.commons.event.EventStore
import net.corda.workbench.commons.event.Filter
import net.corda.workbench.commons.registry.Registry
import net.corda.workbench.transactionBuilder.CordaAppConfig
import net.corda.workbench.transactionBuilder.CordaAppLoader2
import net.corda.workbench.transactionBuilder.booleanQueryParam
import net.corda.workbench.transactionBuilder.clients.AgentQueryImpl
import net.corda.workbench.transactionBuilder.events.Repo

import org.json.JSONObject
import org.slf4j.Logger
import rx.functions.Action1
import java.util.*
import javax.servlet.http.HttpServletResponse


class FlowApi(private val registry: Registry) {
    private val networkManager: String = "http://corda-local-network:1114"
    private val logger: Logger = loggerFor<FlowApi>()
    private val repo = registry.retrieve(Repo::class.java)


    fun register() {
        val app = registry.retrieve(Javalin::class.java)
        ApiBuilder.path(":network/:node/:app") {

            app.routes {
                ApiBuilder.get("flows/list") { ctx ->

                    val appConfig = lookupAppConfig(ctx)
                    if (appConfig != null) {
                        logger.debug("Listing flows for {}, using package {}", appConfig.name, appConfig.scannablePackages[0])
                        val extractor = FlowMetaDataExtractor(appConfig.scannablePackages[0])
                        ctx.json(extractor.availableFlows())
                    } else {
                        ctx.json(Collections.EMPTY_LIST)
                    }
                }
                ApiBuilder.path("flows/:name") {
                    app.routes {
                        ApiBuilder.post("run") { ctx ->
                            val nodeConfig = lookupNodeConfig(ctx)

                            val appConfig = lookupAppConfig(ctx)!!


                            val helper = RPCHelper("corda-local-network:${nodeConfig.port}")
                            helper.connect()
                            val client = helper.cordaRPCOps()!!
                            val resolver = RpcPartyResolver(helper)

                            // todo - fix to pass multiple packages
                            val runner = FlowRunner(appConfig.scannablePackages[0],
                                    resolver,
                                    LiveRpcCaller(client),
                                    Reporter(ctx.response()))

                            val data = JSONObject(ctx.body()).toMap()

                            try {
                                val result = runner.run<Any>(ctx.param("name")!!, data)
                                ctx.json(mapOf("success" to true, "result" to result))
                            } catch (ex: Exception) {
                                ctx.json(mapOf("success" to false, "message" to ex.message))
                            }

                        }

                        ApiBuilder.get("metadata") { ctx ->
                            val appConfig = lookupAppConfig(ctx)!!
                            val extractor = FlowMetaDataExtractor(appConfig.scannablePackages[0])

                            if (ctx.booleanQueryParam("all")) {
                                val result = extractor.allConstructorMetaData(ctx.param("name")!!)
                                ctx.json(result)

                            } else {
                                val result = extractor.primaryConstructorMetaData(ctx.param("name")!!)
                                ctx.json(result)
                            }

                        }


                        ApiBuilder.get("annotations") { ctx ->
                            val appConfig = lookupAppConfig(ctx)!!
                            val extractor = FlowMetaDataExtractor(appConfig.scannablePackages[0])

                            val result = extractor.flowAnnotations(ctx.param("name")!!)
                            ctx.json(result)
                        }
                    }
                }
            }
        }
    }

    private fun lookupNodeConfig(ctx: Context): NodeConfig {
        val network = ctx.param("network")!!
        val node = ctx.param("node")!!
        val result = khttp.get("$networkManager/$network/nodes/$node/config")

        if (result.statusCode == 200) {
            val json = result.jsonObject
            return NodeConfig(legalName = json.getString("legalName"), port = json.getInt("port"))
        } else {
            throw RuntimeException("Cannot read node config for node:$node on network:$network")
        }
    }

    private fun lookupAppConfig(ctx: Context): CordaAppConfig? {
        val network = ctx.param("network")!!
        val app = ctx.param("app")!!

        val config = repo.cordaAppConfig(network, app)
        logger.debug("app config for {} is {}", app, config)
        return config
    }


}

data class NodeConfig(val legalName: String, val port: Int) {


}


class Reporter(val resp: HttpServletResponse) : Action1<String> {
    override fun call(t: String?) {
        resp.writer.print(t!!)
        resp.writer.print("\n")
        resp.writer.flush()
    }
}
