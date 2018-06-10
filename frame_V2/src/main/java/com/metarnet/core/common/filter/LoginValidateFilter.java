package com.metarnet.core.common.filter;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Administrator on 2015/7/17.
 */
public class LoginValidateFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String globalUniqueID = request.getParameter("globalUniqueID");
        System.out.println(request.getSession().getAttribute("globalUniqueID")+"+Session++++++++++++++++" +request.getSession().getAttribute("globalUniqueUser"));
        if (StringUtils.isNotBlank(globalUniqueID)) {
            System.out.println(request.getSession().getAttribute("globalUniqueID")+"---++Session++++++++++++++++" +request.getSession().getAttribute("globalUniqueUser"));
            request.getSession().setAttribute("globalUniqueID", globalUniqueID);
            request.getSession().setAttribute("globalUniqueUser", "");
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
