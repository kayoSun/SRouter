package com.kayo.srouter.annos;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Kayo
 * 2018/12/15
 * 01:18
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface RouterRule {
    String[] value();
    String desc() default "";
}


