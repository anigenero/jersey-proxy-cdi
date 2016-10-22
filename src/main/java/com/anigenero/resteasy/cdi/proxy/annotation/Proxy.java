package com.anigenero.resteasy.cdi.proxy.annotation;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Qualifier
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Proxy {
}
