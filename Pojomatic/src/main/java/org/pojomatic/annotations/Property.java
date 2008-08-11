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
   *	A {@link Property} is included in everything by default
   */
  public PojomaticPolicy policy() default PojomaticPolicy.DEFAULT;
}
