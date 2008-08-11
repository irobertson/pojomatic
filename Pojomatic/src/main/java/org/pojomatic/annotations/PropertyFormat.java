package org.pojomatic.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.pojomatic.formatter.PropertyFormatter;

/**
 * Specifies formatting information to be used for creating {@code String} representations of properties.
 * @see PropertyFormatter
 */
@Target({FIELD, METHOD})
@Retention(RUNTIME)
@Documented
public @interface PropertyFormat {

  /**
   *	The formatter to use for creating a {@code String} representation.
   */
  public Class<? extends PropertyFormatter> value();

  /**
   *    An optional label for this property.
   */
  public String label() default "";
}
