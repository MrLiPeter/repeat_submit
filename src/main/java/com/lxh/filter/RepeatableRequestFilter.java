package com.lxh.filter;


import com.lxh.request.RepeatableReadRequestWrapper;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RepeatableRequestFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        if(StringUtils.startsWithIgnoreCase(request.getContentType(),"application/json")){
            RepeatableReadRequestWrapper requestWrapper = new RepeatableReadRequestWrapper(request, (HttpServletResponse) servletResponse);
            filterChain.doFilter(requestWrapper,servletResponse);
            return;
        }
        filterChain.doFilter(servletRequest,servletResponse);
    }
}
