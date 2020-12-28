package com.wl.Util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class GetBeanUtil implements ApplicationContextAware {

    private static ApplicationContext context = null;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (GetBeanUtil.context == null) {
            context = applicationContext;
        }
    }

    public static Object getBean(String name) {
        return context.getBean(name);
    }

    public static <T> T getBean(Class<T> c) {
        return context.getBean(c);
    }


    public static ApplicationContext getContext() {
        return context;
    }


}
