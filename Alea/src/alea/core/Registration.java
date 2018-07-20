/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package alea.core;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author dalibor
 */
public class Registration {

    public static void register() {
        File activatedFile = new File("activated");
        if (!activatedFile.exists()) {
            /*
            try {
                URL aleaHomePage = new URL("http://www.fi.muni.cz/~xpodoln/alea/index.php?key=xxx");
                HttpURLConnection conn = (HttpURLConnection) aleaHomePage.openConnection();
                InputStream is = conn.getInputStream();
                java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                String str = s.hasNext() ? s.next() : "";
                System.out.println("STR:"+str);
                if (!str.contains("hits")) {
                    throw new Exception("Could not open expected site content.");
                }
                // OK, connection established
                activatedFile.mkdir();
            } catch (Exception e) {
                // Will try next time
                e.printStackTrace();
            }
             */
        }
    }

}
