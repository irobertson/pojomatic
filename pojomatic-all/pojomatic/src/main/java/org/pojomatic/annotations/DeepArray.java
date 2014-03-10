package org.pojomatic.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

/**
 * Indicates that the annotated property (intended to be of type Object or Object[]) may contain a multi-dimensional
 * array, and if so, the array should be treated as a deep arrays in the fashion of {@link Arrays#deepHashCode(Object[])},
 * {@link Arrays#deepEquals(Object[], Object[])} and {@link Arrays#deepToString(Object[])}.
 * <p>
 * Note that if this annotation is present, then {@link CanBeArray} is automatically inferred.
 *
 * @see CanBeArray
 */
@Target({FIELD, METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DeepArray {}
