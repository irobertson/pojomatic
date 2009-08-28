package org.pojomatic.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.pojomatic.formatter.PojoFormatter;

/**
 * Specifies formatting information to be used for creating {@code String} representations of POJOs.
 * @see PojoFormatter
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface PojoFormat {

  /**
   *    The formatter to use for creating a {@code String} representation.
   */
  public Class<? extends PojoFormatter> value();
}
