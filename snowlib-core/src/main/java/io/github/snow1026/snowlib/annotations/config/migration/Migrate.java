package io.github.snow1026.snowlib.annotations.config.migration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Migrate {
    String from();
    String to();
}
