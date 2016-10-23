package com.anigenero.resteasy.cdi.proxy;

import com.anigenero.resteasy.cdi.proxy.annotation.ResteasyProxy;
import com.anigenero.resteasy.cdi.proxy.annotation.Proxy;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;

public class ProxyBean implements Bean, Serializable {

    private static final Logger logger = Logger.getLogger(ProxyBean.class.toString());

    private static final String HOST_SUFFIX = "host";
    private static final String PASSWORD_SUFFIX = "password";
    private static final String PORT_SUFFIX = "port";
    private static final String TIMEOUT_SUFFIX = "timeout";
    private static final String URL_SUFFIX = "url";
    private static final String USERNAME_SUFFIX = "username";

    private static final int DEFAULT_PORT = 80;
    private static final int DEFAULT_TIMEOUT = 5000;

    private final Class<Type> type;

    /**
     * Creates the proxy bean with the specified type
     *
     * @param type {@link Class} of {@link Type}
     */
    public ProxyBean(Class<Type> type) {
        this.type = type;
    }

    @Override
    public Set<Type> getTypes() {
        return new HashSet<>(Collections.singleton(this.type));
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return new HashSet<>(Arrays.asList((Annotation) () -> Proxy.class, () -> Default.class, () -> Any.class));
    }

    @Override
    public Class<Dependent> getScope() {
        return Dependent.class;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<Object> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public Class<Type> getBeanClass() {
        return this.type;
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public Set<Object> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public Object create(CreationalContext creationalContext) {
        return getProxy(getBeanClass());
    }

    @Override
    public void destroy(Object instance, CreationalContext creationalContext) {
        creationalContext.release();
    }

    /**
     * Gets the proxy bean instance from the interface
     *
     * @param proxyClass {@link Class} the proxy class
     * @return <T>
     */
    private <T> T getProxy(Class<T> proxyClass) {

        ResteasyProxy proxyAnnotation = proxyClass.getAnnotation(ResteasyProxy.class);

        String proxyName = proxyAnnotation.name();
        String host = getProxyValue(proxyName, HOST_SUFFIX, null);
        if (host == null || host.isEmpty()) {
            logger.log(Level.SEVERE, "Could not create proxy for '" + proxyAnnotation.name() + "' because no host is set");
            return null;
        }

        ProxyConfiguration proxyConfiguration = new ProxyConfiguration();

        try {
            if (!ClientRequestFilter.class.equals(proxyAnnotation.requestFilter())) {
                proxyConfiguration.setRequestFilter(proxyAnnotation.requestFilter().newInstance());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not instantiate ClientRequestFilter for proxy '" + proxyName + "'", e);
        }

        try {
            if (!ClientResponseFilter.class.equals(proxyAnnotation.responseFilter())) {
                proxyConfiguration.setResponseFilter(proxyAnnotation.responseFilter().newInstance());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not instantiate ClientResponseFilter for proxy '" + proxyName + "'", e);
        }

        proxyConfiguration.setHost(host);
        proxyConfiguration.setUrlPrefix(getProxyValue(proxyName, URL_SUFFIX, null));
        proxyConfiguration.setTimeout(getProxyValue(proxyName, TIMEOUT_SUFFIX, DEFAULT_TIMEOUT));
        proxyConfiguration.setPort(getProxyValue(proxyName, PORT_SUFFIX, DEFAULT_PORT));

        proxyConfiguration.setUsername(getProxyValue(proxyName, USERNAME_SUFFIX, null));
        proxyConfiguration.setPassword(getProxyValue(proxyName, PASSWORD_SUFFIX, null));
        proxyConfiguration.setCredentialsProvider(proxyAnnotation.credentialsProvider());

        return createProxy(proxyClass, proxyConfiguration);

    }

    /**
     * Gets the proxy value as an integer
     *
     * @param proxyName      {@link String} the name of the proxy. This is used for configuration
     *                       purposes
     * @param propertySuffix {@link String} the property suffix
     * @param defaultValue   int - the default value
     * @return int - the parsed value
     */
    private int getProxyValue(String proxyName, String propertySuffix, int defaultValue) {

        String value = getProxyValue(proxyName, propertySuffix, null);
        if (value != null && !value.isEmpty()) {
            return Integer.parseInt(value);
        } else {
            return defaultValue;
        }

    }

    /**
     * Gets the proxy value as a string
     *
     * @param proxyName      {@link String} the name of the proxy. This is used for configuration
     *                       purposes
     * @param propertySuffix {@link String} the property suffix
     * @param defaultValue   {@link String} the default value
     * @return {@link String} the value of the property
     */
    private String getProxyValue(String proxyName, String propertySuffix, String defaultValue) {

        try {

            String propertyName = String.format("restproxy.%s.%s", proxyName, propertySuffix);
            String value = System.getProperty(propertyName);

            if (value != null && !value.isEmpty()) {
                return value;
            } else {
                return defaultValue;
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not createProxy the proxy value '" + propertySuffix + "' because of an error: " + e.getMessage());
            return defaultValue;
        }

    }

    /**
     * Creates the proxy
     *
     * @param proxyClass    {@link Class} the proxy class
     * @param configuration {@link ProxyConfiguration}
     * @return T
     */
    private <T> T createProxy(Class<T> proxyClass, ProxyConfiguration configuration) {

        // createProxy the URL and ensure that it's not empty
        String url = proxyClass.getAnnotation(ResteasyProxy.class).urlPrefix();
        if (url.isEmpty()) {
            logger.log(Level.SEVERE, "Unable to create RESTEasy proxy " + proxyClass.getSimpleName() + ": Missing URL pefix");
            return null;
        }

        generateUrlPrefix(configuration);

        try {

            RequestConfig.Builder requestBuilder = RequestConfig.custom();

            buildConnectionTimeouts(requestBuilder, configuration);

            HttpClientBuilder builder = HttpClientBuilder.create();
            builder.setDefaultRequestConfig(requestBuilder.build());

            // configure the authentication (if applicable)
            configureAuthentication(builder, configuration);
            // configure the client builder
            createClientBuilder(builder, configuration);

            return createClientBuilder(builder, configuration).build().target(configuration.getUrlPrefix()).proxy(proxyClass);

        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Unable to create proxy " + proxyClass.getSimpleName() + ": " + e.getMessage(), e);
            return null;
        }

    }

    /**
     * Generates the URL prefix for the proxy
     *
     * @param configuration {@link ProxyConfiguration}
     */
    private void generateUrlPrefix(ProxyConfiguration configuration) {

        String urlPrefix = configuration.getUrlPrefix();
        if (urlPrefix != null) {
            if (!urlPrefix.startsWith("/")) {
                urlPrefix = "/" + urlPrefix;
            }
        } else {
            urlPrefix = "";
        }

        configuration.setUrlPrefix(urlPrefix);

    }

    /**
     * Builds the connection timeouts for the proxy
     *
     * @param requestBuilder {@link RequestConfig.Builder}
     * @param configuration  {@link ProxyConfiguration}
     */
    private void buildConnectionTimeouts(RequestConfig.Builder requestBuilder, ProxyConfiguration configuration) {

        requestBuilder.setConnectTimeout(configuration.getTimeout() / 2);
        requestBuilder.setConnectionRequestTimeout(configuration.getTimeout() / 2);

        if (configuration.getHost() != null && configuration.getPort() > 1 && configuration.getPort() <= 65536) {

            HttpHost httpHost = new HttpHost(configuration.getHost(), configuration.getPort());
            requestBuilder.setProxy(httpHost);

            configuration.setUrlPrefix(httpHost.toString() + configuration.getUrlPrefix());

        }

    }

    /**
     * Configures the authentication for the proxy, if applicable (username and password are set)
     *
     * @param builder       {@link HttpClientBuilder}
     * @param configuration {@link ProxyConfiguration}
     */
    private void configureAuthentication(HttpClientBuilder builder, ProxyConfiguration configuration) {

        if (configuration.getUsername() != null && configuration.getPassword() != null) {

            // get the username and password credentials
            Credentials credentials = new UsernamePasswordCredentials(configuration.getUsername(),
                    configuration.getPassword());

            try {

                CredentialsProvider credentialsProvider = configuration.getCredentialsProvider().newInstance();
                credentialsProvider.setCredentials(new AuthScope(configuration.getHost(), configuration.getPort()),
                        credentials);

                builder.setDefaultCredentialsProvider(credentialsProvider);

            } catch (InstantiationException | IllegalAccessException e) {
                logger.log(Level.SEVERE, "Could not create credentials provider because of an error", e);
            }


        }

    }

    /**
     * Creates the client builder
     *
     * @param httpClientBuilder {@link HttpClientBuilder}
     * @param configuration     {@link ProxyConfiguration}
     * @return {@link ResteasyClientBuilder}
     */
    private ResteasyClientBuilder createClientBuilder(HttpClientBuilder httpClientBuilder, ProxyConfiguration configuration) {

        ResteasyClientBuilder resteasyClientBuilder = new ResteasyClientBuilder();
        resteasyClientBuilder.httpEngine(new ApacheHttpClient4Engine(httpClientBuilder.build()));

        if (configuration.getRequestFilter() != null) {
            resteasyClientBuilder.register(configuration.getRequestFilter());
        }

        if (configuration.getResponseFilter() != null) {
            resteasyClientBuilder.register(configuration.getResponseFilter());
        }

        return resteasyClientBuilder;

    }

}