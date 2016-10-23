package com.anigenero.resteasy.cdi.proxy;

import com.anigenero.resteasy.cdi.proxy.annotation.ResteasyProxy;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProxyExtension implements Extension {

    private static final Logger logger = Logger.getLogger(ProxyExtension.class.toString());

    private Set<Bean> proxyBeanSet = new HashSet<>();

    /**
     * Observes bean discovery and creates beans of all the proxy types
     *
     * @param processAnnotatedType {@link ProcessAnnotatedType}
     * @param <T>                  the proxy class type
     */
    @SuppressWarnings("unchecked")
    public <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> processAnnotatedType) {

        // if this isn't an interface or doesn't have a @ResteasyProxy annotation, there's nothing for us to do
        Class<T> proxyClass = processAnnotatedType.getAnnotatedType().getJavaClass();
        if (!proxyClass.isInterface() || !proxyClass.isAnnotationPresent(ResteasyProxy.class)) {
            return;
        }

        logger.log(Level.INFO, "Adding proxy '" + processAnnotatedType.getAnnotatedType().getJavaClass() + "'");

        // create the bean and add it
        proxyBeanSet.add(new ProxyBean((Class<Type>) processAnnotatedType.getAnnotatedType().getBaseType()));

    }

    /**
     * Add all the created beans from the ProxyExtension#processAnnotatedType into the
     * {@link BeanManager}
     *
     * @param afterBeanDiscovery {@link AfterBeanDiscovery}
     */
    public void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery) {
        proxyBeanSet.forEach(afterBeanDiscovery::addBean);
    }

}
