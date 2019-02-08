package net.corda.workbench.transactionBuilder.agent.api

import io.javalin.ApiBuilder
import io.javalin.Context
import io.javalin.Javalin
import net.corda.core.contracts.ContractState
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.reflections.app.RPCHelper
import net.corda.reflections.reflections.LiveRpcCaller
import net.corda.reflections.reflections.StateMetaDataExtractor
import net.corda.reflections.resolvers.UniqueIdentifierResolver
import net.corda.workbench.commons.registry.Registry
import net.corda.workbench.transactionBuilder.CordaAppConfig
import net.corda.workbench.transactionBuilder.CordaClassLoader
import net.corda.workbench.transactionBuilder.events.Repo
import java.util.*


class QueryApi(private val registry: Registry) {

    val networkManager = "http://corda-local-network:1114"
    val repo = registry.retrieve(Repo::class.java)


    fun register() {
        val app = registry.retrieve(Javalin::class.java)


        ApiBuilder.path(":network/:node/:app") {

            app.routes {
                ApiBuilder.get("states/list") { ctx ->
                    val appConfig = lookupAppConfig(ctx)
                    if (appConfig != null) {
                        val extractor = StateMetaDataExtractor(appConfig.scannablePackages[0])
                        ctx.json(extractor.availableStates())
                    }
                    else {
                        ctx.json(Collections.EMPTY_LIST)
                    }
                }
                ApiBuilder.path("query") {
                    app.routes {
                        ApiBuilder.get(":state") { ctx ->
                            val config = lookupNodeConfig(ctx)
                            val clazzName = ctx.param("state")!!

                            val appConfig = lookupAppConfig(ctx)!!
                            val clazz = lookupClass<ContractState>(clazzName,appConfig.name)

                            // todo - need to inject in the correct IP address - simple case only works on dev
                            val helper = RPCHelper("corda-local-network:${config.port}")
                            helper.connect()
                            val client = helper.cordaRPCOps()!!
                            val caller = LiveRpcCaller(client)


                            val queryCriteria = QueryCriteria.LinearStateQueryCriteria(contractStateTypes = setOf(clazz))
                            val result = caller.vaultQueryBy(criteria = queryCriteria, contractStateType = clazz)
                            ctx.json(result.states.reversed().map { it.state.data })
                        }
                    }

                    app.routes {
                        ApiBuilder.get(":state/:linearId") { ctx ->
                            val config = lookupNodeConfig(ctx)
                            val clazzName = ctx.param("state")!!
                            val appConfig = lookupAppConfig(ctx)!!
                            val clazz = lookupClass<ContractState>(clazzName,appConfig.name)

                            // todo - need to inject in the correct IP address - simple case only works on dev
                            val helper = RPCHelper("corda-local-network:${config.port}")
                            helper.connect()
                            val client = helper.cordaRPCOps()!!
                            val caller = LiveRpcCaller(client)

                            val idParam = ctx.param("linearId")!!

                            val id = UniqueIdentifierResolver().resolveValue(idParam)!!
                            val externalIds = if (id.externalId != null) {
                                listOf(id.externalId!!)
                            } else {
                                null
                            }

                            val queryCriteria = QueryCriteria.LinearStateQueryCriteria(
                                    contractStateTypes = setOf(clazz),
                                    uuid = listOf(id.id),
                                    externalId = externalIds,
                                    status =  Vault.StateStatus.ALL
                            )
                            val result = caller.vaultQueryBy(criteria = queryCriteria,
                                    contractStateType = clazz)
                            ctx.json(result.states.reversed().map { it.state.data })
                        }
                    }
                }
            }
        }
    }

    fun lookupNodeConfig(ctx: Context): NodeConfig {
        val network = ctx.param("network")!!
        val node = ctx.param("node")!!
        val result = khttp.get("$networkManager/$network/nodes/$node/config")

        if (result.statusCode == 200) {
            val json = result.jsonObject
            return NodeConfig(legalName = json.getString("legalName"), port = json.getInt("port"))
        } else {
            throw RuntimeException("Cannot read node config for node:'$node' on network:'$network'")
        }
    }

    fun lookupAppConfig(ctx: Context): CordaAppConfig? {
        val network = ctx.param("network")!!
        val app = ctx.param("app")!!
        return repo.cordaAppConfig(network,app)

    }

    private inline fun <reified T> lookupClass(className: String, appName : String): Class<T> {

        return CordaClassLoader().lookupClass(className, appName + ".jar")
    }


    data class NodeConfig(val legalName: String, val port: Int)

}