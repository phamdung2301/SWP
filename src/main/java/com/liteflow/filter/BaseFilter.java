package com.liteflow.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * BaseFilter: cung cấp tiện ích chung để giảm trùng lặp
 */
public abstract class BaseFilter implements Filter {

    protected HttpServletRequest asHttp(ServletRequest req) {
        return (HttpServletRequest) req;
    }

    protected HttpServletResponse asHttp(ServletResponse res) {
        return (HttpServletResponse) res;
    }

    protected HttpSession getSession(HttpServletRequest req, boolean create) {
        return req.getSession(create);
    }

    protected String getPath(HttpServletRequest req) {
        String ctx = req.getContextPath();
        String uri = req.getRequestURI();
        return (ctx != null && !ctx.isEmpty()) ? uri.substring(ctx.length()) : uri;
    }
}
