package com.anigenero.resteasy.cdi.proxy;

import com.anigenero.resteasy.cdi.proxy.annotation.ResteasyProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

public class ProxyProvider implements Extension {

    private static final Logger log = LogManager.getLogger(ProxyProvider.class);

    private Set<Bean> proxyBeanSet = new HashSet<>();

    /**
     * Observes bean discovery and creates beans of all the proxy types
     *
     * @param processAnnotatedType {@link ProcessAnnotatedType}
     */
    @SuppressWarnings("unchecked")
    public <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> processAnnotatedType) {

        // if this isn't an interface or doesn't have a @Path annotation, there's nothing for us to do
        Class<T> proxyClass = processAnnotatedType.getAnnotatedType().getJavaClass();
        if (!proxyClass.isInterface() || !proxyClass.isAnnotationPresent(ResteasyProxy.class)) {
            return;
        }

        log.info("Adding proxy '{}'", processAnnotatedType.getAnnotatedType().getJavaClass());

        // create the bean and add it
        proxyBeanSet.add(new ProxyBean((Class<Type>) processAnnotatedType.getAnnotatedType().getBaseType()));

    }

    /**
     * Add all the created beans from the ProxyProvider#processAnnotatedType into the
     * {@link BeanManager}
     *
     * @param afterBeanDiscovery {@link AfterBeanDiscovery}
     */
    public void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery) {
        proxyBeanSet.forEach(afterBeanDiscovery::addBean);
    }

}
