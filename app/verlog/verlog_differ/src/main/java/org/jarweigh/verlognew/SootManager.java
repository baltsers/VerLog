package org.jarweigh.verlognew;

import org.jarweigh.verlog.data.CallgraphPair;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SootManager {

    private SootManager() {
        throw new IllegalStateException("Utility class");
    }

    public static Set<SootMethod> getSrcCodeMethodsInSootClass(SootClass sootClass) {
        Set<SootMethod> srcCodeMethods = new HashSet<>();
        for (SootMethod sootMethod : sootClass.getMethods()) {
            if (sootMethod.isPhantom()) {
                continue;
            }
            if (sootMethod.getSignature().contains("access$")) {
                continue;
            }
            if (sootMethod.getSignature().contains("lambda$")) {
                continue;
            }
            srcCodeMethods.add(sootMethod);
        }
        return srcCodeMethods;
    }

    private static boolean sootParameterTypeEqualsJavaParserParameterType(String sootParameterType, String javaParserParameterType) {
        //Convert all the $ in the sootParameterType to .
        sootParameterType = sootParameterType.replace("$", ".");
        // Remove <> from javaParserParameterType
        javaParserParameterType = javaParserParameterType.replaceAll("<.*>", "");
        // Soot parameter type: android.location.Location <-> JavaParser parameter type: Location
        // or int <-> int
        return sootParameterType.equals(javaParserParameterType) || sootParameterType.endsWith(javaParserParameterType);
    }

    private static boolean sootMethodParametersEqualsJavaParserParameters(List<Type> sootParameterTypes, List<String> javaParserParameters) {
        try {
            if (sootParameterTypes.size() != javaParserParameters.size()) {
                return false;
            }
            for (int i = 0; i < sootParameterTypes.size(); i++) {
                if (!sootParameterTypeEqualsJavaParserParameterType(sootParameterTypes.get(i).toString(), javaParserParameters.get(i))) {
                    return false;
                }
            }
            return true;
        } catch (NullPointerException e) {
            return false;
        }

    }

    public static boolean sootMethodEqualsJavaParserMethod(SootMethod sootMethod, MethodPos methodPos) {
        // In Java, the method signature is the method name and the parameters
        if (!sootMethod.getName().equals(methodPos.getMethodName())) {
            return false;
        }
        return sootMethodParametersEqualsJavaParserParameters(sootMethod.getParameterTypes(), methodPos.getParameters());
    }

    public static Set<String> getCallees(String methodSignature) {
        // Get the callees of the method from the call graph
        CallGraph cg = CallgraphPair.currentCallgraph;
        Set<String> callees = new HashSet<>();
        Iterator<Edge> edges = cg.edgesOutOf(Scene.v().getMethod(methodSignature));
        while (edges.hasNext()) {
            SootMethod targetMethod = edges.next().tgt();
            callees.add(targetMethod.getSignature());
        }
        return callees;
    }

}
