package org.nexus_lab.relf.testload.core;

import org.atteo.classindex.IndexAnnotated;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IndexAnnotated
@Retention(RetentionPolicy.RUNTIME)
public @interface AppMeta {
    String name();
    String packageName();
    int sdk() default -1;
    int minSdk() default Integer.MIN_VALUE;
    int maxSdk() default Integer.MAX_VALUE;
}
