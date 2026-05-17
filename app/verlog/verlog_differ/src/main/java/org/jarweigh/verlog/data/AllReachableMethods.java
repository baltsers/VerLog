package org.jarweigh.verlog.data;

import java.util.HashSet;
import java.util.Set;

public class AllReachableMethods {
    //Singleton
    private static AllReachableMethods instance = null;
    private final Set<String> allReachableMethods;

    private AllReachableMethods(){
        allReachableMethods = new HashSet<>();
    }

    public static AllReachableMethods getInstance(){
        if(instance == null){
            instance = new AllReachableMethods();
        }
        return instance;
    }

    public void addMethod(String methodSignature){
        allReachableMethods.add(methodSignature);
    }

    public boolean isInAllReachableMethods(String methodSignature){
        return allReachableMethods.contains(methodSignature);
    }
}
