package com.lxh.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RepeatSubmit {

    /**
     * 两个请求直接的间隔时间
     * @return
     */
    int interval() default 5000;

    String message() default "不允许重复提交,请稍候再试";
}
