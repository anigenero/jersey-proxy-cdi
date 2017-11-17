package com.anigenero.jersey.proxy;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.inject.Scope;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("WeakerAccess")
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

        // if this isn't an interface or doesn't have a @RestProxy annotation, there's nothing for us to do
        Class<T> proxyClass = processAnnotatedType.getAnnotatedType().getJavaClass();
        if (!proxyClass.isInterface() || !proxyClass.isAnnotationPresent(RestProxy.class)) {
            return;
        }

        Class<? extends Annotation> beanScope = Dependent.class;
        Optional<Annotation> scope = determineScope(processAnnotatedType.getAnnotatedType());
        if (scope.isPresent()) {
            beanScope = scope.get().getClass();
        }

        logger.log(Level.INFO, "Adding proxy '" + processAnnotatedType.getAnnotatedType().getJavaClass() + "'");

        // create the bean and add it
        proxyBeanSet.add(new ProxyBean((Class<Type>) processAnnotatedType.getAnnotatedType().getBaseType(), beanScope));

    }

    public <T> Optional<Annotation> determineScope(AnnotatedType<T> annotatedType) {
        return annotatedType.getAnnotations().stream().filter(annotation -> annotation.annotationType().getAnnotation(Scope.class) != null).findFirst();
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