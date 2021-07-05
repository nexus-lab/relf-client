package org.nexus_lab.relf.client.actions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for define the permissions a action requires.
 * <p>
 * This is different from {@link androidx.annotation.RequiresPermission} as this annotation
 * can and only can be placed on class elements, and may contain required permissions and
 * optional permissions at the same time.
 *
 * @author Ruipeng Zhang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface UsesPermission {
    /**
     * The name of the permission that is required. If the action needs more than one requests, use
     * {@link #allOf()} instead.
     *
     * @return See {@link android.Manifest.permission}
     */
    String value() default "";

    /**
     * The names of permissions that all must be satisfied.
     *
     * @return See {@link android.Manifest.permission}
     */
    String[] allOf() default {};

    /**
     * The names of permissions that at least one should be satisfied.
     *
     * @return See {@link android.Manifest.permission}
     */
    String[] anyOf() default {};
}
