package com.cunae.cdi.resteasy.proxy;

import com.cunae.cdi.resteasy.proxy.annotation.Proxy;
import com.cunae.cdi.resteasy.proxy.annotation.ResteasyProxy;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;

public class ProxyBean implements Bean, Serializable {

    private static final Logger log = LogManager.getLogger(ProxyBean.class);

    private static final String HOST_SUFFIX = "host";
    private static final String PASSWORD_SUFFIX = "password";
    private static final String PORT_SUFFIX = "port";
    private static final String TIMEOUT_SUFFIX = "timeout";
    private static final String URL_SUFFIX = "url";
    private static final String USERNAME_SUFFIX = "username";

    private static final int DEFAULT_PORT = 80;
    private static final int DEFAULT_TIMEOUT = 5000;

    private final Class<Type> type;

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
            log.error("Could not create proxy for {} because no host is set", proxyClass);
            return null;
        }

        ProxySettings proxySettings = new ProxySettings();

        try {
            if (!ClientRequestFilter.class.equals(proxyAnnotation.requestFilter())) {
                proxySettings.setRequestFilter(proxyAnnotation.requestFilter().newInstance());
            }
        } catch (Exception e) {
            log.error("Could not instantiate ClientRequestFilter {}", proxyName, e);
        }

        try {
            if (!ClientResponseFilter.class.equals(proxyAnnotation.responseFilter())) {
                proxySettings.setResponseFilter(proxyAnnotation.responseFilter().newInstance());
            }
        } catch (Exception e) {
            log.error("Could not instantiate ClientResponseFilter for {}", proxyName, e);
        }

        proxySettings.setHost(host);
        proxySettings.setUrlPrefix(getProxyValue(proxyName, URL_SUFFIX, null));
        proxySettings.setTimeout(getProxyValue(proxyName, TIMEOUT_SUFFIX, DEFAULT_TIMEOUT));
        proxySettings.setPort(getProxyValue(proxyName, PORT_SUFFIX, DEFAULT_PORT));

        proxySettings.setUsername(getProxyValue(proxyName, USERNAME_SUFFIX, null));
        proxySettings.setPassword(getProxyValue(proxyName, PASSWORD_SUFFIX, null));

        return createProxy(proxyClass, proxySettings);

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
            log.warn("Could not createProxy the proxy value {} because of an error: {}", propertySuffix,
                    e.getMessage());
            return defaultValue;
        }

    }

    /**
     * Creates the proxy
     *
     * @param proxyClass    {@link Class} the proxy class
     * @param proxySettings {@link ProxySettings}
     * @return T
     */
    private <T> T createProxy(Class<T> proxyClass, ProxySettings proxySettings) {

        // createProxy the URL and ensure that it's not empty
        String url = proxyClass.getAnnotation(ResteasyProxy.class).urlPrefix();
        if (url.isEmpty()) {
            log.error("Unable to create RESTeasy proxy {}: Missing @Path URL", proxyClass.getSimpleName());
            return null;
        }

        String urlPrefix = proxySettings.getUrlPrefix();
        if (urlPrefix != null) {
            if (!urlPrefix.startsWith("/")) {
                urlPrefix = "/" + urlPrefix;
            }
        } else {
            urlPrefix = "";
        }

        try {

            RequestConfig.Builder requestBuilder = RequestConfig.custom();
            requestBuilder.setConnectTimeout(proxySettings.getTimeout() / 2);
            requestBuilder.setConnectionRequestTimeout(proxySettings.getTimeout() / 2);

            if (proxySettings.getHost() != null && proxySettings.getPort() > 1 && proxySettings.getPort() <= 65536) {

                HttpHost proxy = new HttpHost(proxySettings.getHost(), proxySettings.getPort());
                requestBuilder.setProxy(proxy);
                urlPrefix = proxy.toString() + urlPrefix;

            }

            HttpClientBuilder builder = HttpClientBuilder.create();
            builder.setDefaultRequestConfig(requestBuilder.build());

            // configure the authentication (if applicable)
            configureAuthentication(builder, proxySettings);

            ResteasyClientBuilder resteasyClientBuilder = new ResteasyClientBuilder();
            resteasyClientBuilder.httpEngine(new ApacheHttpClient4Engine(builder.build()));

            if (proxySettings.getRequestFilter() != null) {
                resteasyClientBuilder.register(proxySettings.getRequestFilter());
            }

            if (proxySettings.getResponseFilter() != null) {
                resteasyClientBuilder.register(proxySettings.getResponseFilter());
            }

            ResteasyWebTarget target = resteasyClientBuilder.build().target(urlPrefix);
            return target.proxy(proxyClass);

        } catch (Throwable e) {
            log.error("Unable to create proxy {}: {}", proxyClass.getSimpleName(), e.getMessage(), e);
            return null;
        }

    }

    /**
     * Configures the authentication for the proxy, if applicable (username and password are set)
     *
     * @param builder       {@link HttpClientBuilder}
     * @param proxySettings {@link ProxySettings}
     */
    private void configureAuthentication(HttpClientBuilder builder, ProxySettings proxySettings) {

        if (proxySettings.getUsername() != null && proxySettings.getPassword() != null) {

            Credentials credentials = new UsernamePasswordCredentials(proxySettings.getUsername(),
                    proxySettings.getPassword());

            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(new AuthScope(proxySettings.getHost(), proxySettings.getPort()),
                    credentials);

            builder.setDefaultCredentialsProvider(credentialsProvider);

        }

    }

    private static class ProxySettings {

        private ClientRequestFilter requestFilter;
        private ClientResponseFilter responseFilter;

        private String host;
        private String urlPrefix;
        private String username;
        private String password;

        private int port;
        private int timeout;

        public ClientRequestFilter getRequestFilter() {
            return requestFilter;
        }

        public void setRequestFilter(ClientRequestFilter requestFilter) {
            this.requestFilter = requestFilter;
        }

        public ClientResponseFilter getResponseFilter() {
            return responseFilter;
        }

        public void setResponseFilter(ClientResponseFilter responseFilter) {
            this.responseFilter = responseFilter;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getUrlPrefix() {
            return urlPrefix;
        }

        public void setUrlPrefix(String urlPrefix) {
            this.urlPrefix = urlPrefix;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
    }

}