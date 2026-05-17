package org.jarweigh.verlognew;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SourceCodeManager {

    private SourceCodeManager() {
        throw new IllegalStateException("Utility class");
    }

//    private static String getChangedJavaFilePath(String filePathInRepo) {
//        return Globals.gitRepo + "/" + filePathInRepo;
//    }

    private static String getRemovedJavaFilePath(String filePathInRepo) {
        return Globals.refVersionRepo + "/" + filePathInRepo;
    }

    private static String getAddedJavaFilePath(String filePathInRepo) {
        return Globals.tgtVersionRepo + "/" + filePathInRepo;
    }

    private static Set<MethodPos> getAllMethodPosInOneFile(String filePath) throws FileNotFoundException {
        return MethodExtractor.getAllNonAnonymousMethods(filePath, new HashSet<>());
    }

    public static Set<MethodPos> getAllMethodPosInAddedClass(String addedClsPathInRepo) throws FileNotFoundException {
        return getAllMethodPosInOneFile(getAddedJavaFilePath(addedClsPathInRepo));
    }

    public static Set<MethodPos> getAllMethodPosInRemovedClass(String removedClsPathInRepo) throws FileNotFoundException {
        return getAllMethodPosInOneFile(getRemovedJavaFilePath(removedClsPathInRepo));
    }

    public static ChangedMethods getChangedMethodPosInOneFile(String oldFilePath, String newFilePath) throws FileNotFoundException {
        oldFilePath = getRemovedJavaFilePath(oldFilePath);
        newFilePath = getAddedJavaFilePath(newFilePath);
        Set<MethodPos> removedMethodPos = new HashSet<>();
        Set<MethodPos> addedMethodPos = new HashSet<>();
        Set<MethodPos> modifiedMethodRefPos = new HashSet<>();
        Set<MethodPos> modifiedMethodTgtPos = new HashSet<>();


        // Get all methods from both files
        Set<MethodInfo> oldMethods = MethodExtractor.getAllNonAnonymousMethodsWithBody(oldFilePath, new HashSet<>());
        Set<MethodInfo> newMethods = MethodExtractor.getAllNonAnonymousMethodsWithBody(newFilePath, new HashSet<>());

        // Find added methods
        Set<MethodInfo> addedMethods = new HashSet<>(newMethods);
        addedMethods.removeAll(oldMethods);
        addedMethods.forEach(m -> addedMethodPos.add(m.getMethodPos()));
        addedMethods.forEach(m -> System.out.println("Method added in " + m.getMethodPos().getClassName() + ": " + m.getMethodPos().getSignature() + " at line " + m.getMethodPos().getStartLine() + "- " + m.getMethodPos().getEndLine() + " in the new file"));
        // Find removed methods
        Set<MethodInfo> removedMethods = new HashSet<>(oldMethods);
        removedMethods.removeAll(newMethods);
        removedMethods.forEach(m -> removedMethodPos.add(m.getMethodPos()));
        removedMethods.forEach(m -> System.out.println("Method removed: in " + m.getMethodPos().getClassName() + ": " + m.getMethodPos().getSignature() + " at line " + m.getMethodPos().getStartLine() + "- " + m.getMethodPos().getEndLine() + " in the old file"));

        // Find modified methods
        Set<MethodInfo> intersectionMethods = new HashSet<>(oldMethods);
        intersectionMethods.retainAll(newMethods);

        for (MethodInfo method : intersectionMethods) {
            MethodInfo oldMethod = oldMethods.stream().filter(m -> m.equals(method)).findFirst().get();
            MethodInfo newMethod = newMethods.stream().filter(m -> m.equals(method)).findFirst().get();
            if (!oldMethod.getMethodBody().equals(newMethod.getMethodBody())) {
                modifiedMethodRefPos.add(oldMethod.getMethodPos());
                modifiedMethodTgtPos.add(newMethod.getMethodPos());
                System.out.println("Method modified: in " + newMethod.getMethodPos().getClassName() + ": "+ method.getMethodPos().getSignature() + " at line " + method.getMethodPos().getStartLine() + "- " + method.getMethodPos().getEndLine() + " in the new file");
            }
        }
        return new ChangedMethods(addedMethodPos, modifiedMethodRefPos, modifiedMethodTgtPos, removedMethodPos);

    }





}
