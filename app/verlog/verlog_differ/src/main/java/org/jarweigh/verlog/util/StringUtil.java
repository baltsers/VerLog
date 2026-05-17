package org.jarweigh.verlog.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringUtil {

    // Usage: <org.woheller69.eggtimer.MainActivity: void onPause()> -> org.woheller69.eggtimer.MainActivity
    public static String getClassSignatureFromMethodSignature(String methodSignature) {
        int colonPos = methodSignature.indexOf(":");
        return methodSignature.substring(1, colonPos);
    }

    public static String getMethodNameFromMethodSignature(String methodSignature) {
        // Usage: <org.woheller69.eggtimer.MainActivity: void onPause()> -> onPause
        String[] methodSignatureSplits = methodSignature.split(" ");
        String tmpSignature = methodSignatureSplits[methodSignatureSplits.length - 1];
        int leftBracketIdx = tmpSignature.indexOf("(");
        return tmpSignature.substring(0, leftBracketIdx);
    }

    public static String convertSootMethodSignatureToJavaParserSignature(String sootMethodSignature) {
        String[] methodSignatureSplits = sootMethodSignature.split(" ");
        String methodName = methodSignatureSplits[methodSignatureSplits.length - 1];
        // Get the parameters
        String paramBody = sootMethodSignature.substring(sootMethodSignature.lastIndexOf("(") + 1, sootMethodSignature.lastIndexOf(")"));
        // if method has multiple parameters
        List<String> params = getMethodParams(paramBody);
        String javaParserSignature = methodName.substring(0, methodName.indexOf("(")) + "(";
        for (int i = 0; i < params.size(); i++) {
            if (i == params.size() - 1) {
                javaParserSignature += params.get(i) + ")";
                break;
            }
            javaParserSignature += params.get(i) + ", ";
        }
        return javaParserSignature;
    }

    private static List<String> getMethodParams(String paramBody) {
        List<String> params = new ArrayList<>();
        if (paramBody.contains(",")) {
            String[] paramSplits = paramBody.split(",");
            //Arrays.stream(paramSplits).forEach(System.out::println);
            for (int i = 0; i < paramSplits.length; i++) {
                String paramFullType = paramSplits[i];
                // if parameter is a class
                String paramType = paramFullType;
                if (paramFullType.contains(".")) {
                    String[] paramParts = paramFullType.split("\\.");
                    paramType = paramParts[paramParts.length - 1];
                }
                params.add(paramType);
            }
        } else {
            // if method has only one parameter
            String paramType = paramBody;
            if (paramBody.contains(".")) {
                String[] paramParts = paramBody.split("\\.");
                paramType = paramParts[paramParts.length - 1];
            }
            params.add(paramType);
        }
        return params;
    }

    public static String getClassNameFromClassSignature(String classSignature) {
        // ca.cmetcalfe.locationshare.MainActivity -> MainActivity
        return classSignature.substring(classSignature.lastIndexOf(".") + 1);
    }

    public static String getNormalizedJimpleStatement(String jimpleStatement) {
        jimpleStatement = jimpleStatement.replaceAll("\\$\\$Lambda\\$[a-zA-Z0-9\\-\\$]+", "LAMBDA_CLASS");
        jimpleStatement = jimpleStatement.replaceAll("\\d{7,}", "RESOURCE_ID");
        jimpleStatement = jimpleStatement.replaceAll("-LAMBDA_CLASS[_a-zA-Z0-9\\-]*", "LAMBDA_CLASS");
        return jimpleStatement;
    }

    public static String getVersionTagFromAppPath(String apkPath) {
        String[] pathParts = apkPath.split("/");
        // app path: a/b/c/v/xxx.apk -> version tag: v
        return pathParts[pathParts.length - 2];
    }
}
