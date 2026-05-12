package com.liteflow.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

// @WebFilter annotation removed - using web.xml mapping to control filter order
// This filter is redundant with CharacterEncodingFilter in web.xml, but kept for logging
public class CommonFilter extends BaseFilter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpServletRequest req = asHttp(request);
        HttpServletResponse res = asHttp(response);

        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            System.out.printf("[CommonFilter] %s %s -> %d (%d ms)%n",
                    req.getMethod(),
                    req.getRequestURI(),
                    res.getStatus(),
                    duration);
        }
    }
}
