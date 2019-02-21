package net.corda.reflections.annotations.facade

/*
* Additional metadata for higher level modelling tool that map CorDapps to a simple
* Data Facade pattern, that is a flatten model of all the internal state. For example,
 * this is used by the Microsoft Workbench integrations, which require a simple
 * "flat" data model.
*/

/**
 * A "marker" annotation which indicates the flow returns a simple structure
 * holding the current data for the contract. The choice of structure is implementation
 * specific, but the idea is always the same, a client can call a single "query" style
 * flow that will assemble data form 1 or more state in the format required by the client.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class DataFacade()
