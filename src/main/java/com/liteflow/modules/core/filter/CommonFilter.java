package com.liteflow.modules.core.filter;

import com.liteflow.filter.BaseFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Logging + encoding; mapping trong {@code web.xml} cùng thứ tự với CharacterEncodingFilter.
 */
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
