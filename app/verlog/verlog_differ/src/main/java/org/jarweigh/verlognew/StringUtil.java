package org.jarweigh.verlognew;

public class StringUtil {

    private StringUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static String getMethodNameFromJavaParserSignature(String javaParserSignature) {
        // Example: "updateLocation(Location)" -> "updateLocation"
        String[] parts = javaParserSignature.split("\\(");
        return parts[0];
    }

//    public static boolean compareJavaParserSignatureWithSootSignature(String javaParserSignature, String sootSignature) {
//
//    }




}
