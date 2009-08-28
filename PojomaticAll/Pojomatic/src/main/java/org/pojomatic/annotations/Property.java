package org.pojomatic.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.pojomatic.Pojomatic;

/**
 * Marks a property of a class to be used by {@link Pojomatic}
 * @see PojomaticPolicy
 */
@Target({FIELD, METHOD})
@Retention(RUNTIME)
@Documented
public @interface Property {

  /**
   * Which sets of {@link Pojomatic} operations ({@code equals}, {@code hashCode} and
   * {@code toString}) should use a property.
   */
  public PojomaticPolicy policy() default PojomaticPolicy.DEFAULT;

  /**
   * The name used to identify the property in the standard {@code toString} representation.  If
   * empty, the following algorithm is used to determine the name.  For a propertiy referenced by
   * field, the name of the field is used.  For a property referenced by a method whose name is of
   * the form {@code getSomeField}, the name {@code someField} will be used.  For a boolean property
   * referenced by a method whose name is of the form {@code isSomeField}, the name
   * {@code someField} will be used.  For any other property referenced by a method, the name of
   * the method is used.
   */
  public String name() default "";
}
