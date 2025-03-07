package kr.co.metabuild.kotris.config;

import org.springframework.context.ApplicationContext;

public class BeanContext {

    private static ApplicationContext context;

    public static void init(ApplicationContext context){ // 1. applicationContext 를 주입받을 메서드
        BeanContext.context = context;
    }

    public static <T> T get(Class<T> clazz){
        return context.getBean(clazz);
    }

    public static <T> T get(String name, Class<T> clazz){
        return context.getBean(name, clazz);
    }

}
