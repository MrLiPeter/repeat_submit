package com.lxh.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lxh.annotation.RepeatSubmit;
import com.lxh.filter.RepeatableRequestFilter;
import com.lxh.redis.RedisCache;
import com.lxh.request.RepeatableReadRequestWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class RepeatSubmitInterceptor implements HandlerInterceptor {

    public static final String REPEAT_PARAMS = "repeat_params";
    public static final String REPEAT_TIME = "repeat_time";
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit_key";
    public static final String HEADER = "Authorization";

    @Autowired
    RedisCache redisCache;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        System.out.println("request.getReader().readLine()"+request.getReader().readLine());
        if(handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            RepeatSubmit repeatSubmit = method.getAnnotation(RepeatSubmit.class);
            if(repeatSubmit != null){
                if(isRepeatSubmit(request,repeatSubmit)) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", "500");
                    map.put("message", repeatSubmit.message());
                    response.setContentType("application/json;charset=utf-8");
                    response.getWriter().write(new ObjectMapper().writeValueAsString(map));
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isRepeatSubmit(HttpServletRequest request, RepeatSubmit repeatSubmit) {
        //请求参数字符串
        String nowParams = "";
        if(repeatSubmit instanceof RepeatableReadRequestWrapper){
            try {
                nowParams = request.getReader().readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //否则说明请求参数是key-value格式的
        if(StringUtils.isEmpty(nowParams)){
            try{
                nowParams = new ObjectMapper().writeValueAsString(request.getParameterMap());
            }catch (JsonProcessingException e){
                e.printStackTrace();
            }
        }
        Map<String,Object> nowDataMap = new HashMap<>();
        nowDataMap.put(REPEAT_PARAMS,nowParams);
        nowDataMap.put(REPEAT_TIME,System.currentTimeMillis());
        String requestURI = request.getRequestURI();
        String header = request.getHeader(HEADER);
        String cachekey = REPEAT_SUBMIT_KEY + requestURI + header.replace("Bearer ","");
        Object cacheObject = redisCache.getCacheObject(cachekey);
        if(cacheObject != null){
            Map<String,Object> map = (Map<String,Object>) cacheObject;
            if(compareParms(map,nowDataMap) && compareTime(map,nowDataMap,repeatSubmit.interval())){
                return true;
            }
        }
        redisCache.setCacheObject(cachekey,nowDataMap,
                repeatSubmit.interval(),
                TimeUnit.MILLISECONDS);
        return false;
    }

    /**
     * 判断是否重复提交
     * @param map
     * @param nowDataMap
     * @param interval
     * @return
     */
    private boolean compareTime(Map<String, Object> map, Map<String, Object> nowDataMap, int interval) {
        Long time1 = (Long)map.get(REPEAT_TIME);
        Long time2 = (Long)nowDataMap.get(REPEAT_TIME);
        if(time2-time1<interval){
            return true;
        }
        return false;
    }

    private boolean compareParms(Map<String, Object> map, Map<String, Object> nowDataMap) {
        String nowParams = nowDataMap.get(REPEAT_PARAMS).toString();
        String dataParams = map.get(REPEAT_PARAMS).toString();
        return nowParams.equals(dataParams);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }
}
