package com.aftertime.ratallofyou;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class getVersionFromtxt {
    public static String getVersion() {
        try (InputStream in = Main.class.getClassLoader().getResourceAsStream("version.txt")) {
            if (in == null) return "unknown";
            return new BufferedReader(new InputStreamReader(in)).readLine().trim();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
