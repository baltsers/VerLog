package org.jarweigh.verlog.service;

import com.google.common.collect.Iterators;
import org.jarweigh.verlog.data.CallgraphPair;
import org.jarweigh.verlog.data.ChangedMethods;
import org.jarweigh.verlog.util.MethodFilter;
import org.jarweigh.verlog.util.StringUtil;
import soot.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SootMethodService {

    private SootMethodService() {
        throw new IllegalStateException("Utility class");
    }

    public static List<SootMethod> retrieveMethodsInSootClass(String clsName){
        Set<String> methodsInCls = new HashSet<>();
        // Make sure the current Scene is for the app that has this class
        SootClass cls = Scene.v().loadClassAndSupport(clsName);
        List<SootMethod> sootMethodList = cls.getMethods();
        List<SootMethod> userMethodList = sootMethodList.stream()
                .filter(sootMethod -> !sootMethod.isPhantom())
                .filter(sootMethod -> !sootMethod.isAbstract())
                .filter(sootMethod -> !sootMethod.getSignature().contains("setIntent(android.content.Intent)"))
                .filter(sootMethod -> !sootMethod.getSignature().contains("getIntent()"))
                .filter(sootMethod -> !sootMethod.getSignature().contains("setResult(int,android.content.Intent)"))
                .filter(sootMethod -> !MethodFilter.isLambdaMethod(sootMethod.getSignature()))
                .collect(Collectors.toList());
//        for (SootMethod sootMethod : sootMethodList) {
//            methodsInCls.add(sootMethod.getSignature());
//        }
        return userMethodList;
    }


    public static ChangedMethods getChangedMethods(Set<SootMethod> refMethods, Set<SootMethod> tgtMethods){
        Set<SootMethod> addedMethods = new HashSet<>();
        Set<SootMethod> modifiedMethodsRef = new HashSet<>();
        Set<SootMethod> modifiedMethodsTgt = new HashSet<>();
        Set<SootMethod> deletedMethods = new HashSet<>();
        // find deleted and modified
        SootMethod matchedMethodInTgt = null;
        for (SootMethod methodInRef : refMethods) {
            matchedMethodInTgt = SootMethodService.getMethodBySignature(methodInRef.getSignature(), tgtMethods);

            // If two methods are actually the same, but with different method names due to obfuscation
            // We treat them as one deleted (from v1) and added (to v2)
            if (matchedMethodInTgt == null) {
                deletedMethods.add(methodInRef);
            } else {
               //Compare method body (statement by statement) to determine if they are modified
                if (!SootMethodService.twoMethodsStrictlyEqual(methodInRef, matchedMethodInTgt)) {
                    modifiedMethodsTgt.add(matchedMethodInTgt);
                    modifiedMethodsRef.add(methodInRef);
                }
            }

        }

        // find added methods
        for (SootMethod methodInVj : tgtMethods) {
            if (SootMethodService.getMethodBySignature(methodInVj.getSignature(), refMethods) == null) {
                addedMethods.add(methodInVj);
            }
        }

        return new ChangedMethods(addedMethods, modifiedMethodsRef, modifiedMethodsTgt, deletedMethods);

    }

    // Lightweight comparison of two methods, "resilient" to obfuscation
    public static boolean twoMethodsEqual(SootMethod s1, SootMethod s2) {
        // Compare the incoming and outgoing edges of the two methods in the call graph
        int inEdgeS1 = Iterators.size(CallgraphPair.refCallGraph.edgesInto(s1));
        int inEdgeS2 = Iterators.size(CallgraphPair.tgtCallGraph.edgesInto(s2));
        int outEdgeS1 = Iterators.size(CallgraphPair.refCallGraph.edgesOutOf(s1));
        int outEdgeS2 = Iterators.size(CallgraphPair.tgtCallGraph.edgesOutOf(s2));
//        System.out.println(s1 + ": " + inEdgeS1 + " " + outEdgeS1);
//        System.out.println(s2 + ": " + inEdgeS2 + " " + outEdgeS2);
        return inEdgeS1 == inEdgeS2 && outEdgeS1 == outEdgeS2;
    }

    private static SootMethod getMethodBySignature(String signature, Set<SootMethod> methods) {
        SootMethod matchedMethod = null;
        for (SootMethod method : methods) {
            if (method.getSignature().equals(signature)) {
                matchedMethod = method;
                break;
            }
        }
        return matchedMethod;
    }

    // compare two methods statement by statement
    private static boolean twoMethodsStrictlyEqual(SootMethod s1, SootMethod s2) {
        if (s1.isAbstract() && s2.isAbstract()) {
            return true;
        }
        Body b1 = s1.retrieveActiveBody();
        Body b2 = s2.retrieveActiveBody();
        UnitPatchingChain unitsInS1 = b1.getUnits();
        UnitPatchingChain unitsInS2 = b2.getUnits();
        if (unitsInS1.size() != unitsInS2.size()) {
            return false;
        }
        if (b1.getLocalCount() != b2.getLocalCount()) {
            return false;
        }
        if (b1.getTraps().size() != b2.getTraps().size()) {
            return false;
        }
        Iterator<Unit> itS1 = unitsInS1.iterator();
        Iterator<Unit> itS2 = unitsInS2.iterator();
        while (itS1.hasNext() && itS2.hasNext()) {
            Unit unitS1 = itS1.next();
            Unit unitS2 = itS2.next();
            // Normalize the unit string to ignore the resource id
            String normalizedUnitS1 = StringUtil.getNormalizedJimpleStatement(unitS1.toString());
            String normalizedUnitS2 = StringUtil.getNormalizedJimpleStatement(unitS2.toString());


            if (!normalizedUnitS1.equals(normalizedUnitS2)) {
                return false;
            }
        }
        return true;
    }

}
