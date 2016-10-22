package com.anigenero.resteasy.cdi.proxy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface ResteasyProxy {

    /**
     * Sets the name of the proxy that will be used to reference configuration
     * @return {@link String}
     */
    String name();

    /**
     * Sets the base path of the proxy (e.g. "/v1")
     * @return {@link String}
     */
    String urlPrefix();

    /**
     * Sets the request filter
     * @return {@link Class} extends {@link ClientRequestFilter}
     */
    Class<? extends ClientRequestFilter> requestFilter() default ClientRequestFilter.class;

    /**
     * Sets the response filter
     * @return {@link Class} extends {@link ClientResponseFilter}
     */
    Class<? extends ClientResponseFilter> responseFilter() default ClientResponseFilter.class;

}
