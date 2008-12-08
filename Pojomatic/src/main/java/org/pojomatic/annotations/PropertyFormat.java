package org.pojomatic.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.pojomatic.formatter.PropertyFormatter;

/**
 * Specifies formatting information to be used for creating {@code String} representations of
 * properties.  Note that using a {@code PropertyFormat} annotation on a property does not influence
 * whether that property will be included in the {@code toString} implementation.  That is
 * determined solely by any {@link Property} and {@link AutoProperty} annotations on the POJO.
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
}
