package org.pojomatic.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.pojomatic.Pojomatic;
import org.pojomatic.formatter.DefaultPropertyFormatter;
import org.pojomatic.formatter.PropertyFormatter;

/**
 * Marks a property of a class to be used by {@link Pojomatic}
 * @see PojomaticPolicy
 */
@Target({FIELD, METHOD})
@Retention(RUNTIME)
@Inherited
@Documented
public @interface Property {

  /**
   *	A {@link Property} is included in everything by default
   */
  public PojomaticPolicy policy() default PojomaticPolicy.DEFAULT;

  /**
   *	The formatter to use for {@link Pojomatic#toString(Object)}
   */
  public Class<? extends PropertyFormatter> formatter() default DefaultPropertyFormatter.class;

  /**
   *    An optional label
   */
  public String label() default "";
}
