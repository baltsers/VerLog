package org.jarweigh.verlog.service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;
import java.util.stream.Collectors;
import org.jarweigh.verlog.data.MethodPos;
import org.jarweigh.verlog.util.StringUtil;
import soot.SootMethod;
import soot.util.HashMultiMap;
import soot.util.MultiMap;

public class SourceCodeService {
    // public static void main(String[] args) {
    //     String filePath = "../Dataset/SuntimesWidget/app/src/main/java/com/forrestguice/suntimeswidget/SuntimesActivity.java";


    // }

    private SourceCodeService() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get the line numbers of all methods in the source code file.
     *
     * @param cu the CompilationUnit object of the source code file
     * @return a set of MethodPos objects, each of which contains the method name, start line number, and end line number
     */
    private static Set<MethodPos> getMethodsLineNum(CompilationUnit cu) {
        try {
            return cu.findAll(MethodDeclaration.class).stream()
                    .map(method -> {
                        // String methodName = method.getName().asString();
                        String methodSignature = method.getSignature().asString();
                        int startLine = method.getBegin().get().line;
                        int endLine = method.getEnd().get().line;
                        return new MethodPos(methodSignature, startLine, endLine);
                    }).collect(Collectors.toSet());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        // Return null if the source code file is not found
        return null;

    }


    /**
     * Get the line numbers of all methods in the source code file.
     *
     * @param srcCode the source code file as a string
     * @return a set of MethodPos objects, each of which contains the method name, start line number, and end line number
     */
    private static Set<MethodPos> getMethodsLineNumFromStr(String srcCode) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(srcCode);
            return getMethodsLineNum(cu);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        // Return null if the source code file is not found
        return null;
    }

    

    /**
     * Get the line numbers of all methods in the source code file.
     *
     * @param srcCodeFile the source code file
     * @return a set of MethodPos objects, each of which contains the method name, start line number, and end line number
     */
    private static Set<MethodPos> getMethodsLineNumFromFile(String srcCodeFile) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(new File(srcCodeFile));
            return getMethodsLineNum(cu);
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        }
        // Return null if the source code file is not found
        return null;
    }

    public static MultiMap<String, MethodPos> convertToMethodPos(Set<String> changedClasses, String versionTag) {
        MultiMap<String, MethodPos> clsWithMethodPos = new HashMultiMap<>();
        //changedClasses.stream().forEach(System.out::println);
        try{
            for (String changedCls : changedClasses) {
                String classContentPath = GitMiningService.getSrcCodePathOfClass(versionTag, StringUtil.getClassNameFromClassSignature(changedCls));
                Set<MethodPos> methodsLineNum = getMethodsLineNumFromFile(classContentPath);
                clsWithMethodPos.putAll(changedCls, methodsLineNum);
            }
            return clsWithMethodPos;
        }  catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return null;
    }

}
