package net.corda.workbench.cordaNetwork

import java.net.InetAddress

/**
 * The holder for any application wide configuration / settings
 */

data class AppConfig(val publicAddress: InetAddress)