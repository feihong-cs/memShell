package com.memshell.generic;

import sun.misc.BASE64Encoder;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Printer {

    public static void main(String[] args) throws IOException {

        String path = "G:\\code\\java\\memShell\\target\\classes\\com\\memshell\\generic\\DynamicFilterTemplate.class";
        FileInputStream in = new FileInputStream(path);
        byte[] bytes = new byte[in.available()];
        in.read(bytes);

        FileOutputStream stream = new FileOutputStream("111.class");
        stream.write(bytes);
        stream.close();

        BASE64Encoder encoder = new BASE64Encoder();
        String base64String = encoder.encode(bytes).replaceAll("\r\n|\r|\n","");
        System.out.println(base64String);
        in.close();
    }
}
