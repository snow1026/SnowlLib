package io.github.snow1026.snowlib.annotation.config;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigKey {
    String value();
    String[] alias() default {};
    String def() default "";
    boolean saveDefault() default true;
    String comment() default "";
}
