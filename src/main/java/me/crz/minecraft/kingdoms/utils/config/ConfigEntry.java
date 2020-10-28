package me.crz.minecraft.kingdoms.utils.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ConfigEntry {
    String[] old() default {};

    boolean reload() default true;

    String parser() default "";
}
