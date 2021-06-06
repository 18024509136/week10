package com.geek;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 为@Service注解的类中@RpcReference注解的属性注入代理类
 */
@Component
public class RpcStubAnnotationPostProcessor implements BeanPostProcessor {

    @Autowired
    private RpcClientCglibProxy cglibProxy;

    private static final Map<String, RpcReference> REFERENCE_CONFIG_CACHE = new ConcurrentHashMap<>(10);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        // 只考虑处理@Service注解的类
        if (clazz.isAnnotationPresent(Service.class)) {
            Field[] declaredFields = bean.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                // 只考虑处理@RpcReference注解的属性
                if (field.isAnnotationPresent(RpcReference.class)) {
                    field.setAccessible(true);

                    // 生成代理对象
                    Object proxyBean = cglibProxy.createProxy(field.getType());
                    try {
                        // 属性设置为代理对象
                        field.set(bean, proxyBean);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    // 将rpc接口对应的注解缓存起来，以便后续取到注解相关属性
                    REFERENCE_CONFIG_CACHE.put(field.getType().getName(), field.getAnnotation(RpcReference.class));
                }
            }
            return bean;
        }
        return bean;
    }

    public static RpcReference getRpcClientConfig(String rpcClientApiName) {
        return REFERENCE_CONFIG_CACHE.get(rpcClientApiName);
    }
}
