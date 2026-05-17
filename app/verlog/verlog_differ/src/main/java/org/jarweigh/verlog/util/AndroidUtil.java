package org.jarweigh.verlog.util;

import org.xmlpull.v1.XmlPullParserException;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

import java.io.IOException;

public class AndroidUtil {
    public static String getPackageName(String apkPath) throws XmlPullParserException, IOException{
        ProcessManifest manifest = new ProcessManifest(apkPath);
        return manifest.getPackageName();
    }
}
