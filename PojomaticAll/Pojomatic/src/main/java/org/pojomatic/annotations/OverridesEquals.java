package org.pojomatic.annotations;

import java.lang.annotation.*;

/**
 * Declares that the annotated type overrides the behavior of {@link Object#equals(Object) equals}.
 * This can serve as a hint to
 * {@link org.pojomatic.Pojomatic#equals(Object, Object)} Pojomatic.equals(pojo, other) to defer
 * to {@code other.equals(this)} in certain cases.
 * @see SubclassCanOverrideEquals 
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OverridesEquals {
}
