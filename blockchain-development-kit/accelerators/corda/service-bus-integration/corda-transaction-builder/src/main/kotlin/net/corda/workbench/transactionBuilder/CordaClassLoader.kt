package net.corda.workbench.transactionBuilder

import io.github.classgraph.ClassGraph
import net.corda.core.utilities.loggerFor
import org.slf4j.Logger
import java.util.*
import io.github.classgraph.ClassInfo


class CordaClassLoader(val customerLoader: ClassLoader? = null) {

    companion object {
        private val logger: Logger = loggerFor<CordaAppLoader>()
    }

    inline fun <reified T> lookupClass(name: String, jarFileName: String?): Class<T> {
        val classGraph = ClassGraph()
                .enableAllInfo()

        if (customerLoader != null) {
            classGraph.addClassLoader(customerLoader)
        }

        val results = ArrayList<ClassInfo>()

        // TODO - should be getting the scannable packages names for the app and not hardcoding
        classGraph
                .whitelistPackages("net.corda", "com.r3")
                .scan()
                .use { scanResult ->
                    scanResult.allClasses
                            .filter { it.simpleName == name }
                            .filter { it.classpathElementFile != null }
                            .toCollection(results)
                }

        if (jarFileName != null) {
            val clazz = results.filter { it.classpathElementFile != null }
                    .single { it.classpathElementFile.name.endsWith(jarFileName) }
                    .name
            return Class.forName(clazz) as Class<T>


        } else {
            val clazz = results
                    .single().name
            return Class.forName(clazz) as Class<T>
        }
    }


}
