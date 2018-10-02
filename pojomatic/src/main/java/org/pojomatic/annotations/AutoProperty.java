package org.pojomatic.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.pojomatic.Pojomatic;

/**
 * Assigns the defaults for {@link Pojomatic} at the class level and provides a way to
 * configure the automatic detection of properties.
 * Note that this can is overridden (case by case) by the {@link Property}} annotation.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface AutoProperty {
  /**
   * Which sets of {@link Pojomatic} operations ({@code equals}, {@code hashCode} and
   * {@code toString}) should use properties, unless otherwise stated by {@link Property @Property}.
   * @return Which operations should properties by default.
   */
  public DefaultPojomaticPolicy policy() default DefaultPojomaticPolicy.ALL;

  /**
   * Specifies whether to auto-detect properties by their fields, getters or not at all.
   * @return whether to auto-detect properties by their fields, getters or not at all.
   */
  public AutoDetectPolicy autoDetect() default AutoDetectPolicy.FIELD;
}
