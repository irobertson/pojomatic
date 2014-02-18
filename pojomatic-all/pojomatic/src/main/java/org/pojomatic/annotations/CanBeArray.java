package org.pojomatic.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated property (intended to be of type Object) may contain an array, and if so, should be
 * treated as such by the various methods of {@link Pojomator}.
 * <p>
 * Note that if the annotated property is already annotated
 * with @{@link DeepArray}, then this annotation need not be added as well.
 *
 * @see {@link DeepArray}
 */
@Target({FIELD, METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CanBeArray {}
