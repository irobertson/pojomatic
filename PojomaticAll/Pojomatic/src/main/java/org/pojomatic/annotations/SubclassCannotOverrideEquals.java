package org.pojomatic.annotations;

import java.lang.annotation.*;

import org.pojomatic.Pojomator;

/**
 * Declares that a subclass of the annotated type cannot override the behavior of equals.
 *
 * Abent this annotation, it is assumed that subclasses cannot override {@code equals} for interface
 * types, and can for other types.
 *
 * @see Pojomator#doEquals(Object, Object)
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubclassCannotOverrideEquals {
}
