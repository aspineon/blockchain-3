package net.corda.reflections.annotations

/**
 * Common annotations. Valid for use in any CorDapp
 */

/**
 * A basic description that can be applied as necessary
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION,
        AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Description(val text: String)



