package com.inject.ndh;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ndh on 16/6/15.
 */
public class Utils {
    public static byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            byte[] b = new byte[4096];
            boolean n = false;

            int n1;
            while((n1 = is.read(b)) != -1) {
                System.out.print("n1="+n1);
                output.write(b, 0, n1);
            }

            byte[] var4 = output.toByteArray();
            return var4;
        } finally {
            output.close();
        }
    }
}
