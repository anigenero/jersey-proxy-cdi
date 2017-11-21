package com.anigenero.jersey.proxy;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ProxyBean implements Bean, Serializable {

    private static final Logger log = LogManager.getLogManager().getLogger(ProxyBean.class.getName());

    private final Class<Type> type;
    private final Class<? extends Annotation> beanScope;

    /**
     * Creates the proxy bean with the specified type
     *
     * @param type      {@link Class} of {@link Type}
     * @param beanScope {@link Class} of {@link Annotation}
     */
    @SuppressWarnings("WeakerAccess")
    public ProxyBean(Class<Type> type, Class<? extends Annotation> beanScope) {
        this.beanScope = beanScope;
        this.type = type;
    }

    @Override
    public Set<Type> getTypes() {
        return new HashSet<>(Collections.singleton(this.type));
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return new HashSet<>(Arrays.asList((Annotation) () -> Default.class, () -> Any.class));
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return this.beanScope;
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


        ResourceProxy proxyAnnotation = proxyClass.getAnnotation(ResourceProxy.class);

        final String proxyName = proxyAnnotation.name();
        String url = getProxyValue(proxyAnnotation.url());
        if (url == null || url.isEmpty()) {
            log.log(Level.SEVERE, "Could not create proxy for '" + proxyAnnotation.name() + "' because no url is set");
            return null;
        }

        RestConfiguration.Builder builder = new RestConfiguration.Builder(proxyClass);

        try {
            if (!ClientRequestFilter.class.equals(proxyAnnotation.requestFilter())) {
                builder.setRequestFilter(proxyAnnotation.requestFilter().newInstance());
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Could not instantiate ClientRequestFilter for proxy '" + proxyName + "'", e);
        }

        try {
            if (!ClientResponseFilter.class.equals(proxyAnnotation.responseFilter())) {
                builder.setResponseFilter(proxyAnnotation.responseFilter().newInstance());
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Could not instantiate ClientResponseFilter for proxy '" + proxyName + "'", e);
        }

        builder.setUrl(proxyAnnotation.url());

//        builder.setUsername(getProxyValue(proxyAnnotation));
//        builder.setPassword(getProxyValue(proxyName, PASSWORD_SUFFIX, null));

        builder.setCredentialsProvider(proxyAnnotation.credentialsProvider());

        return createProxy(proxyClass, builder.build());

    }

    /**
     * Gets the proxy value for the specified string
     *
     * @param value {@link String}
     * @return {@link String}
     */
    private String getProxyValue(String value) {
        return value;
    }

    /**
     * Creates the proxy
     *
     * @param proxyClass        {@link Class} the proxy class
     * @param restConfiguration {@link RestConfiguration}
     * @return T
     */
    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> proxyClass, RestConfiguration restConfiguration) {

        // createProxy the URL and ensure that it's not empty
        String url = proxyClass.getAnnotation(ResourceProxy.class).url();
        if (url.isEmpty()) {
            log.log(Level.SEVERE, "Unable to create RESTEasy proxy " + proxyClass.getSimpleName() + ": Missing URL pefix");
            return null;
        }

        return (T) new RestProxyFactory(restConfiguration).build();

    }

}