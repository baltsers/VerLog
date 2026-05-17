package org.jarweigh.verlognew;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Iterators;
import org.jarweigh.verlog.SootConfig;
import org.jarweigh.verlog.data.CallgraphPair;
import org.jarweigh.verlog.data.FlowDroid;
import org.jarweigh.verlog.service.SourceCodeService;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.infoflow.results.InfoflowPerformanceData;
import soot.util.HashMultiMap;
import soot.util.MultiMap;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ContextualizedDiffer {
    public static void run() throws FileNotFoundException {
        // Get the changed classes between the two versions
        long differencingStartTime = System.nanoTime();
        ChangedClasses changedClasses = GitRepoManager.getChangedClasses(Globals.gitRepo, Globals.refVersion, Globals.tgtVersion);
        MultiMap<String, MethodPos>  addedClsMethods = new HashMultiMap<>();
        Map<String, ChangedMethods> modifiedClsMethods = new HashMap<>();
        MultiMap<String, MethodPos> removedClsMethods = new HashMultiMap<>();

        // Get all the methods in the added, removed and modified classes
        for (String addedCls : changedClasses.getAddedClasses()) {
            Set<MethodPos> methodsInAddedCls = SourceCodeManager.getAllMethodPosInAddedClass(addedCls);
            addedClsMethods.putAll(addedCls, methodsInAddedCls);
        }

        for (String removedCls : changedClasses.getRemovedClasses()) {
            Set<MethodPos> methodsInRemovedCls = SourceCodeManager.getAllMethodPosInRemovedClass(removedCls);
            removedClsMethods.putAll(removedCls, methodsInRemovedCls);
        }

        for (String modifiedCls : changedClasses.getModifiedClasses()) {
            // We assume the path modified class is the same in the reference and target versions
            // otherwise, it will be marked as an added or removed class in `git diff`
            ChangedMethods changedMethods = SourceCodeManager.getChangedMethodPosInOneFile(modifiedCls, modifiedCls);
            modifiedClsMethods.put(modifiedCls, changedMethods);
        }
        long differencingEndTime = System.nanoTime();
        PerformanceStat.differencingTimeCost= PerformanceStat.computeExecutionTimeInSeconds(differencingStartTime, differencingEndTime);

        MultiMap<String, ContextualizedMethodPos> addedClsContextualizedMethods = new HashMultiMap<>();
        MultiMap<String, ContextualizedMethodPos> removedClsContextualizedMethods = new HashMultiMap<>();
        Map<String, ChangedContextualizedMethods> modifiedClsContextualizedMethods = new HashMap<>();

        System.out.println("Creating call graphs for the the reference app...");
        long refCallGraphStartTime = System.nanoTime();
        FlowDroid.createCallGraph(Globals.refVersionApkPath);
        long refCallGraphEndTime = System.nanoTime();
        PerformanceStat.refCallGraphTimeCost = PerformanceStat.computeExecutionTimeInSeconds(refCallGraphStartTime, refCallGraphEndTime);
        CallgraphPair.refCallGraph = Scene.v().getCallGraph();
        PerformanceStat.refCallGraphSize = CallgraphPair.refCallGraph.size();
        // SourceMethods are Interator
        CallgraphPair.currentCallgraph = CallgraphPair.refCallGraph;

        for (String modifiedCls : modifiedClsMethods.keySet()) {
            ChangedContextualizedMethods changedContextualizedMethods = new ChangedContextualizedMethods();
            ChangedMethods changedMethods = modifiedClsMethods.get(modifiedCls);
            Set<MethodPos> modifiedMethodsRef = changedMethods.getModifiedMethodsRef();
            Set<MethodPos> removedMethods = changedMethods.getRemovedMethods();
            // Set<MethodPos> -> Set<ContextualizedMethodPos>
            Set<ContextualizedMethodPos> modifiedSootMethodsRef = convertJavaParserMethodsToContextualizedMethods(modifiedMethodsRef);
            Set<ContextualizedMethodPos> removedSootMethods = convertJavaParserMethodsToContextualizedMethods(removedMethods);
            changedContextualizedMethods.setModifiedContextualizedMethodsRef(modifiedSootMethodsRef);
            changedContextualizedMethods.setRemovedContextualizedMethods(removedSootMethods);
            modifiedClsContextualizedMethods.put(modifiedCls, changedContextualizedMethods);
        }
        
        convertJavaParserMethodsToContextualizedMethods(removedClsMethods, removedClsContextualizedMethods);


        // Contextualize the methods in the added, removed and modified classes

        System.out.println("Creating call graphs for the the target app...");
        FlowDroid.createCallGraph(Globals.tgtVersionApkPath);
        CallgraphPair.tgtCallGraph = Scene.v().getCallGraph();
        PerformanceStat.tgtCallGraphSize = CallgraphPair.tgtCallGraph.size();
        CallgraphPair.currentCallgraph = CallgraphPair.tgtCallGraph;

        for (String modifiedCls : modifiedClsMethods.keySet()) {
            ChangedContextualizedMethods changedContextualizedMethods = modifiedClsContextualizedMethods.get(modifiedCls);
            ChangedMethods changedMethods = modifiedClsMethods.get(modifiedCls);
            Set<MethodPos> modifiedMethodsTgt = changedMethods.getModifiedMethodsTgt();
            Set<MethodPos> addedMethods = changedMethods.getAddedMethods();
            // Set<MethodPos> -> Set<ContextualizedMethodPos>
            Set<ContextualizedMethodPos> modifiedSootMethodsTgt = convertJavaParserMethodsToContextualizedMethods(modifiedMethodsTgt);
            Set<ContextualizedMethodPos> addedSootMethods = convertJavaParserMethodsToContextualizedMethods(addedMethods);
            changedContextualizedMethods.setModifiedContextualizedMethodsTgt(modifiedSootMethodsTgt);
            changedContextualizedMethods.setAddedContextualizedMethods(addedSootMethods);
            modifiedClsContextualizedMethods.put(modifiedCls, changedContextualizedMethods);
        }

        convertJavaParserMethodsToContextualizedMethods(addedClsMethods, addedClsContextualizedMethods);
        SerializationManager.serializeAllChangedMethods(addedClsContextualizedMethods, removedClsContextualizedMethods, modifiedClsContextualizedMethods);
    }

    private static Set<ContextualizedMethodPos> convertJavaParserMethodsToContextualizedMethods(Set<MethodPos> javaParserMethods) {
        Set<ContextualizedMethodPos> contextualizedMethodPosSet = new HashSet<>();
        // Source coe method match with the Soot method (to get the reachable methods)
        for (MethodPos javaParserMethod : javaParserMethods) {
            SootClass sc = Scene.v().getSootClass(javaParserMethod.getClassName());
            if (sc == null) {
                System.out.println("Class not found: " + javaParserMethod.getClassName());
                continue;
            }
            boolean methodFound = false;
            for (SootMethod sootMethod : SootManager.getSrcCodeMethodsInSootClass(sc)) {
                //System.out.println("Comparing " + javaParserMethod.getSignature() + " with " + sootMethod.getSignature());
                if (SootManager.sootMethodEqualsJavaParserMethod(sootMethod, javaParserMethod)) {
                    Set<String> callees = SootManager.getCallees(sootMethod.getSignature());
                    ContextualizedMethodPos contextualizedMethodPos = new ContextualizedMethodPos(javaParserMethod,
                            callees,
                            sc.getName(),
                            sootMethod.getSignature());
                    contextualizedMethodPosSet.add(contextualizedMethodPos);
                    methodFound = true;
                    break;
                }
            }
            if (!methodFound) {
                System.out.println("Method not found: " + javaParserMethod.getMethodName() + " in class " + javaParserMethod.getClassName());
            }
        }
        return contextualizedMethodPosSet;
    }

    private static void convertJavaParserMethodsToContextualizedMethods(MultiMap<String, MethodPos> ClsMethods, MultiMap<String, ContextualizedMethodPos> ClsContextMethods) {
        for (String cls : ClsMethods.keySet()) {
            Set<MethodPos> methods = ClsMethods.get(cls);
            Set<ContextualizedMethodPos> contextualizedMethodPos = convertJavaParserMethodsToContextualizedMethods(methods);
            ClsContextMethods.putAll(cls, contextualizedMethodPos);
        }
    }

}
