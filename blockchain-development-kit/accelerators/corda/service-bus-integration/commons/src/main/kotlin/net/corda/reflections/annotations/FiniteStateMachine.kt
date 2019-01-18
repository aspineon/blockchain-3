package net.corda.reflections.annotations.fsm

/**
 * Additional metadata for higher level modelling tool that map CorDapps
 * to a "Finite State Machine" model. For example this is used by the Microsoft Workbench
 * integrations.
 */

/**
 * A "marker" annotation which indicates the flow returns a List<String> representing
 * all the possible finite state machine states
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class FSMStates()


/**
 * A "marker" annotation which indicates the
 * flow returns an String value representing the current contract state
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class FSMCurrentState()


/**
 * A "marker" annotation which indicates the flow returns a list of the
 * available actions (flows) based on the provided state
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class FSMActionsForState()

