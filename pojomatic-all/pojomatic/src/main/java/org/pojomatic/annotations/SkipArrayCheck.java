package org.pojomatic.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If the annotated property is of type {@code Object}, then Pojomatic should not consider the possibility that it could
 * be an array. In particular, if a pair of values are both arrays, they will only be considered equal if they are the
 * same instance. This is primarily intended as a performance improvement in cases where a field of type Object is not
 * expected to contain array values, as it can avoid calls to
 * {@link Object#getClass()}.{@link Class#isArray() isArray()}
 * <p>
 * If the annotated property is not of type {@code Object}, this annotation has no effect.
 *
 * @since 2.0
 */
@Target({FIELD, METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SkipArrayCheck {

}
