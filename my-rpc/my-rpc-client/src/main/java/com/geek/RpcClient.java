package com.geek;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class RpcClient implements InitializingBean {

    @Autowired
    private Discovery discovery;

    @Override
    public void afterPropertiesSet() throws Exception {
        discovery.init();
    }

    /*@Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        try {
            discovery.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
