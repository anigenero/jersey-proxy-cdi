package com.anigenero.jersey.proxy;

import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

import javax.decorator.Decorator;
import javax.enterprise.inject.Stereotype;
import javax.inject.Qualifier;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;
import java.lang.annotation.*;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ResourceProxy {

    /**
     * Sets the name of the proxy that will be used to reference configuration
     *
     * @return {@link String}
     */
    String name();

    /**
     * Gets the host of the
     *
     * @return {@link String}
     */
    String url() default "";

    /**
     * The timeout for the proxy
     *
     * @return int - the timeout, in milliseconds
     */
    int timeout() default -1;

    /**
     * The credentials provider
     *
     * @return {@link Class} of {@link CredentialsProvider}
     */
    Class<? extends CredentialsProvider> credentialsProvider() default BasicCredentialsProvider.class;

    /**
     * Sets the request filter
     *
     * @return {@link Class} extends {@link ClientRequestFilter}
     */
    Class<? extends ClientRequestFilter> requestFilter() default ClientRequestFilter.class;

    /**
     * Sets the response filter
     *
     * @return {@link Class} extends {@link ClientResponseFilter}
     */
    Class<? extends ClientResponseFilter> responseFilter() default ClientResponseFilter.class;

}