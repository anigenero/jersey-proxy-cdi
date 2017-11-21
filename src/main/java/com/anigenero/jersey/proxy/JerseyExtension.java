package com.anigenero.jersey.proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.inject.Scope;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
public class JerseyExtension implements Extension {

    private static final Logger logger = LogManager.getLogger(JerseyExtension.class);

    private Set<Bean> proxyBeanSet = new HashSet<>();

    /**
     * Observes bean discovery and creates beans of all the proxy types
     *
     * @param processAnnotatedType {@link ProcessAnnotatedType}
     * @param <T>                  the proxy class type
     */
    @SuppressWarnings("unchecked")
    protected <T> void processAnnotatedType(@Observes @WithAnnotations({ResourceProxy.class}) ProcessAnnotatedType<T> processAnnotatedType) {

        Class<T> proxyClass = processAnnotatedType.getAnnotatedType().getJavaClass();
        if (!proxyClass.isInterface()) {
            return;
        }

        Class<? extends Annotation> beanScope = Dependent.class;
        Optional<Annotation> scope = determineScope(processAnnotatedType.getAnnotatedType());
        if (scope.isPresent()) {
            beanScope = scope.get().getClass();
        }

        logger.info("Adding proxy '{}'", processAnnotatedType.getAnnotatedType().getJavaClass());

        // create the bean and add it
        proxyBeanSet.add(new ProxyBean((Class<Type>) processAnnotatedType.getAnnotatedType().getBaseType(), beanScope));

    }

    public <T> Optional<Annotation> determineScope(AnnotatedType<T> annotatedType) {
        return annotatedType.getAnnotations().stream().filter(annotation -> annotation.annotationType().getAnnotation(Scope.class) != null).findFirst();
    }

    /**
     * Add all the created beans from the JerseyExtension#processAnnotatedType into the
     * {@link BeanManager}
     *
     * @param afterBeanDiscovery {@link AfterBeanDiscovery}
     */
    protected void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery) {

        if (proxyBeanSet.isEmpty()) {
            logger.warn("No resource proxies found");
            return;
        }

        proxyBeanSet.forEach(afterBeanDiscovery::addBean);

    }

}