package org.jarweigh.verlognew;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.ObjectCreationExpr;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class MethodExtractor {
    //private static Set<MethodInfo> allMethods = new HashSet<>();
    private static Map<String, Integer> anonymousClassCounter = new HashMap<>();

    public static String packageName = "";

    public static String className = "";

    private MethodExtractor() {
        throw new IllegalStateException("Utility class");
    }

    public static Set<MethodPos> getAllNonAnonymousMethods(String filePath, Set<MethodInfo> allMethods) {
        parseJavaFile(filePath, allMethods);
        return allMethods.stream().map(MethodInfo::getMethodPos).collect(Collectors.toSet());
    }

    public static Set<MethodInfo> getAllNonAnonymousMethodsWithBody(String filePath, Set<MethodInfo> allMethods) {
        parseJavaFile(filePath, allMethods);
        return allMethods;
    }

    private static void parseJavaFile(String filePath, Set<MethodInfo> allMethods) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(new File(filePath));
            packageName = cu.getPackageDeclaration().map(pd -> pd.getName().asString()).orElse("");
            className = cu.getPrimaryTypeName().orElse("");
            // Extract methods from all classes (including inner classes)
            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
                try {
                    extractMethods(classDecl, getFullClassName(classDecl), false, allMethods);
                } catch (Exception e) {
                    System.err.println("Error processing class: " + classDecl.getNameAsString());
                    e.printStackTrace();
                }
            });

            // Extract methods from anonymous classes
            //cu.findAll(ObjectCreationExpr.class).forEach(MethodExtractor::extractAnonymousMethods);

            // Print all methods
//            for (MethodInfo method : allMethods) {
//                if (!method.getMethodPos().isAnonymous()) {
//                System.out.printf("Method: %s, Class: %s, Return Type: %s, Parameters: %s, Start Line: %d, End Line: %d%n",
//                        method.getMethodPos().getMethodName(), method.getMethodPos().getClassName(), method.getMethodPos().getReturnType(),
//                        method.getMethodPos().getParameters(), method.getMethodPos().getStartLine(), method.getMethodPos().getEndLine());
//                }
//            }
//            System.out.println("---------------------------------");



        } catch (FileNotFoundException e) {
            System.err.println("Parsing failed: " + e.getMessage());
            System.err.println("Error happened in file: " + filePath);
            //System.err.println("File not found: " + e.getMessage());
        }

    }

    private static void extractMethods(ClassOrInterfaceDeclaration classDecl, String fullClassName, boolean isAnonymous, Set<MethodInfo> allMethods) {
        classDecl.getMethods().forEach(method -> processMethod(method, fullClassName, isAnonymous, allMethods));

        // Handle inner classes
        classDecl.getMembers().stream()
                .filter(member -> member instanceof ClassOrInterfaceDeclaration)
                .map(member -> (ClassOrInterfaceDeclaration) member)
                .forEach(innerClass ->
                        extractMethods(innerClass, fullClassName + "$" + innerClass.getNameAsString(), false, allMethods)
                );
    }

//    private static void extractAnonymousMethods(ObjectCreationExpr anonymousClass) {
//        Optional<NodeList<BodyDeclaration<?>>> bodyDeclarations = anonymousClass.getAnonymousClassBody();
//        if (bodyDeclarations.isPresent()) {
//            String fullClassName = getFullClassName(anonymousClass);
//
//            bodyDeclarations.get().stream()
//                    .filter(body -> body instanceof MethodDeclaration)
//                    .map(body -> (MethodDeclaration) body)
//                    .forEach(method -> processMethod(method, fullClassName, true));
//        }
//    }

    private static void processMethod(MethodDeclaration method, String className, boolean isAnonymous, Set<MethodInfo> allMethods) {
        try {
            String methodName = method.getNameAsString();
            String signature = method.getSignature().asString();
            String returnType = method.getType().asString();
            ArrayList<String> parameters = new ArrayList<>();
            for (Parameter param : method.getParameters()) {
                parameters.add(param.getType().asString());
            }
            int startLine = method.getBegin().get().line;
            int endLine = method.getEnd().get().line;

            MethodPos methodpos = new MethodPos(methodName,
                    signature,
                    packageName + "." + className,
                    returnType,
                    parameters,
                    startLine,
                    endLine,
                    isAnonymous
            );

            String methodBody = method.getBody().map(Object::toString).orElse("");
            MethodInfo methodInfo = new MethodInfo(methodpos, methodBody);
            allMethods.add(methodInfo);
        } catch (Exception e) {
            System.err.println("Error processing method: " + className + ": "+ method.getSignature());
            e.printStackTrace();
        }
    }

    private static String getFullClassName(Node node) {
        List<String> classNames = new ArrayList<>();
        Node currentNode = node;
        String outerClassName = "";

        while (currentNode != null) {
            if (currentNode instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration classDecl = (ClassOrInterfaceDeclaration) currentNode;
                classNames.add(0, classDecl.getNameAsString());
                if (outerClassName.isEmpty()) {
                    outerClassName = classDecl.getNameAsString();
                }
            } else if (currentNode instanceof ObjectCreationExpr) {
                ObjectCreationExpr objCreation = (ObjectCreationExpr) currentNode;
                int anonymousClassNumber = anonymousClassCounter.getOrDefault(outerClassName, 0) + 1;
                anonymousClassCounter.put(outerClassName, anonymousClassNumber);
                classNames.add(0, String.valueOf(anonymousClassNumber));
            }
            currentNode = currentNode.getParentNode().orElse(null);
        }

        return classNames.stream().collect(Collectors.joining("$"));
    }



}