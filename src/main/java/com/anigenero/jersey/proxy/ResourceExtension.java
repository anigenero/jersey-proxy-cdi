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
public class ResourceExtension implements Extension {

    private static final Logger log = LogManager.getLogger(ResourceExtension.class);

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
            log.error("Proxy class {} is not an interface", proxyClass.getName());
            return;
        }

        log.info("Adding proxy '{}'", processAnnotatedType.getAnnotatedType().getJavaClass());

        proxyBeanSet.add(new ResourceProxyBean((Class<Type>) processAnnotatedType.getAnnotatedType().getBaseType(),
                determineScope(processAnnotatedType.getAnnotatedType())));

    }

    /**
     * Determine the scope of the resource proxy. This is done by looping over the scope annotation(s)
     *
     * @param annotatedType {@link AnnotatedType}
     * @param <T>           the annotated class
     * @return {@link Class} of {@link Annotation}
     */
    public <T> Class<? extends Annotation> determineScope(AnnotatedType<T> annotatedType) {

        Optional<Annotation> scope = annotatedType.getAnnotations().stream().filter(annotation -> annotation.annotationType()
                .getAnnotation(Scope.class) != null).findFirst();

        if (scope.isPresent()) {
            return scope.get().getClass();
        } else {
            return Dependent.class;
        }

    }

    /**
     * Add all the created beans from the ResourceExtension#processAnnotatedType into the
     * {@link BeanManager}
     *
     * @param afterBeanDiscovery {@link AfterBeanDiscovery}
     */
    protected void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery) {

        if (proxyBeanSet.isEmpty()) {
            log.warn("No resource proxies found");
            return;
        }

        proxyBeanSet.forEach(afterBeanDiscovery::addBean);

    }

}