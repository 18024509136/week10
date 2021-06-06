package com.geek;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示Rpc客户端存根的注解
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcReference {

    /**
     * 分组名
     *
     * @return
     */
    String group() default "";

    /**
     * 版本号
     *
     * @return
     */
    double version() default 0;
}
