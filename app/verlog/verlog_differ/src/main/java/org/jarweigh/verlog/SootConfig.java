package org.jarweigh.verlog;

import java.io.File;

public class SootConfig {
    public final static String USER_HOME = System.getProperty("user.home");

    public final static String USER_DIR = System.getProperty("user.dir");
    public static String androidJar = USER_HOME + "android-jars";
    static String androidOutPath = System.getProperty("user.dir") + File.separator + "out";
    static String callbackFile = System.getProperty("user.dir") + File.separator + "res/AndroidCallbacks.txt";
    static String srcSinkFile = System.getProperty("user.dir") + File.separator + "res/SourcesAndSinksDroidSafe.txt";


    public static String refAppPath;
    public static String tgtAppPath;

}
