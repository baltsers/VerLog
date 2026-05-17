package org.jarweigh.verlog.service;

import org.jarweigh.verlog.SootConfig;
import org.jarweigh.verlog.data.ChangedMethods;
import org.jarweigh.verlog.util.SootUtil;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.util.HashMultiMap;
import soot.util.MultiMap;

import java.util.*;
import java.util.stream.Collectors;



public class ActivityClsService {

    private ActivityClsService() {
        throw new IllegalStateException("Utility class");
    }

//    public static Set<String> getUserDefinedClasses(String apkPath) {
//        SootUtil.setupSoot(SootConfig.androidJar, apkPath);
//        Set<String> userDefinedClasses = new HashSet<>();
//        Iterator<SootClass> clsIt = Scene.v().getApplicationClasses().iterator();
//        while (clsIt.hasNext()) {
//            SootClass sClass = clsIt.next();
//            if (sClass.isPhantom()) {
//                // skip phantom classes
//                continue;
//            }
//            if (!sClass.isApplicationClass()) {
//                // skip library classes
//                continue;
//            }
//
//            if(sClass.getName().startsWith("androidx") || sClass.getName().startsWith("android")) {
//                continue;
//            }
//
//        }
//    }

    // Get all the activity classes (user-code) of the app in the current Scene
    public static Set<String> getActivityClasses(String apkPath){
        SootUtil.setupSoot(SootConfig.androidJar, apkPath);
        Set<String> activityClassesStr = new HashSet<>();

        Iterator<SootClass> clsIt = Scene.v().getApplicationClasses().iterator();
        while (clsIt.hasNext()) {
            SootClass sClass = clsIt.next();
            if (sClass.isPhantom()) {
                // skip phantom classes
                continue;
            }
            if (!sClass.isApplicationClass()) {
                // skip library classes
                continue;
            }

            if (Scene.v().getSootClass("android.app.Activity").isPhantom()) {
                System.err.println("Error: android.app.Activity class is phantom.");
                continue;
            }

            if(sClass.isInterface()) {
                continue;
            }

            if(sClass.getName().startsWith("androidx") || sClass.getName().startsWith("android")) {
                continue;
            }


            if (Scene.v().getActiveHierarchy().isClassSubclassOf(sClass, Scene.v().getSootClass("android.app.Activity"))) {
                // The class is a user-defined Activity class
                activityClassesStr.add(sClass.getName());
            }

            // Get Fragment classes
            if (Scene.v().getActiveHierarchy().isClassSubclassOf(sClass, Scene.v().getSootClass("androidx.fragment.app.Fragment"))
            || Scene.v().getActiveHierarchy().isClassSubclassOf(sClass, Scene.v().getSootClass("android.app.Fragment"))){
                // The class is a user-defined Fragment class
                activityClassesStr.add(sClass.getName());
            }
        }

        return activityClassesStr;
    }

    public static MultiMap<String, SootMethod> getClsWithMethods(Set<String> classes) {
        MultiMap<String, SootMethod> clsAndMethods = new HashMultiMap<>();
        for (String clsName : classes) {
            clsAndMethods.putAll(clsName, SootMethodService.retrieveMethodsInSootClass(clsName));
        }
        return clsAndMethods;
    }

    public static MultiMap<String, String> getClsWithMethodsSig(Set<String> classes) {
        MultiMap<String, String> clsAndMethods = new HashMultiMap<>();
        for (String clsName : classes) {
            List<String> methodsInCls = SootMethodService.retrieveMethodsInSootClass(clsName).stream()
                    .map(SootMethod::getSignature)
                    .collect(Collectors.toList());
            clsAndMethods.putAll(clsName, methodsInCls);
        }
        return clsAndMethods;
    }



    public static Map<String, ChangedMethods> getDiffMethodsInSameCls(Set<String> sameClasses,
                                                                      MultiMap<String, SootMethod> refMethodsInSameCls,
                                                                      MultiMap<String, SootMethod> tgtMethodsInSameCls) {
        Map<String, ChangedMethods> cmsInModifiedActs = new HashMap<>();
        for (String clsName: sameClasses) {
            ChangedMethods cmIn1Cls = SootMethodService.getChangedMethods(
                    refMethodsInSameCls.get(clsName),
                    tgtMethodsInSameCls.get(clsName)
            );
            if (!cmIn1Cls.isEmpty()) {
                cmsInModifiedActs.put(clsName, cmIn1Cls);
            }
        }
        return cmsInModifiedActs;
    }




}
