package org.jarweigh.verlog.util;

public class MethodFilter {
    public static boolean isAccessMethod(String methodName) {
        return methodName.contains("access$");
    }

    public static boolean isInitMethod(String edgeOrNodeSignature) {
        return edgeOrNodeSignature.contains("<init>") || edgeOrNodeSignature.contains("<clinit>");
    }

    public static boolean isKotlinResultKitMethod(String edgeOrNodeSignature) {
        return edgeOrNodeSignature.contains("kotlin.ResultKt");
    }

    public static boolean isLambdaMethod(String edgeOrNodeSignature) {
        return edgeOrNodeSignature.contains("lambda$") || edgeOrNodeSignature.contains("Lambda$");
    }
}
