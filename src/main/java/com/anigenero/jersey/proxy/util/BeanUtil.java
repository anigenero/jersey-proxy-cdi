package com.anigenero.jersey.proxy.util;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

public class BeanUtil {

    public static <T> T getBean(Class<T> clazz) {

        final BeanManager beanManager = CDI.current().getBeanManager();

        Bean<T> bean = (Bean<T>) beanManager.getBeans(clazz).iterator().next();
        CreationalContext<T> ctx = beanManager.createCreationalContext(bean);
        return (T) beanManager.getReference(bean, clazz, ctx);

    }

}
