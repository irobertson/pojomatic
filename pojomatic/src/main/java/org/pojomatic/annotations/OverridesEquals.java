package org.pojomatic.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pojomatic.Pojomator;

/**
 * Declares that the annotated type overrides the behavior of {@link Object#equals(Object) equals},
 * and hence is not compatible for equals with its superclasses.
 *
 * @see Pojomator#doEquals(Object, Object)
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OverridesEquals {
}
