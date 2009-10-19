package org.pojomatic.annotations;

import java.lang.annotation.*;

/**
 * Declares whether a subclass of the annotated type can override the behavior of equals.  Suppose
 * {@code pojo} is an instance of the annotated class, and {@code other} is an instance of a proper
 * subclass of the annotated class.  If {@code value} is {@code true}, then
 * {@link org.pojomatic.Pojomatic#equals(Object, Object)} Pojomatic.equals(pojo, other)} will defer
 * to {@code other.equals(pojo)} provided that at least one of the following holds for
 * {@code other}'s class, or any superclass of {@code other}'s class which is a proper subclass
 * of the annotated type:
 * <ul >
 *  <li>The class has introduced additioanl properties which are annotated (via either
 * {@link Property @Property} or {@link AutoProperty @AutoProperty}) to take part in the
 * {@code equals} calculation, or
 *  <li>The class is annotated with {@link OverridesEquals}.
 * </ul>
 * If {@code value} is {@code false}, then
 * {@link org.pojomatic.Pojomatic#equals(Object, Object)} Pojomatic.equals(pojo, other)} will never
 * defer to {@code other.equals(this}).
 * <p>
 * Abent this annotation, it is assumed that subclasses cannot override {@code equals} for interface
 * types, and can for other types.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubclassCanOverrideEquals {
  public boolean value() default true;
}
