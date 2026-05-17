package org.jarweigh.verlognew;

import soot.SootMethod;

import java.util.Set;

public class ChangedMethods {
    private final Set<MethodPos> addedMethods;
    private final Set<MethodPos> modifiedMethodsRef;
    private final Set<MethodPos> modifiedMethodsTgt;
    private final Set<MethodPos> removedMethods;

    public ChangedMethods(Set<MethodPos> addedMethods, Set<MethodPos> modifiedMethodsRef, Set<MethodPos> modifiedMethodsTgt, Set<MethodPos> removedMethods) {
        this.addedMethods = addedMethods;
        this.modifiedMethodsRef = modifiedMethodsRef;
        this.modifiedMethodsTgt = modifiedMethodsTgt;
        this.removedMethods = removedMethods;
    }

    public Set<MethodPos> getAddedMethods() {
        return addedMethods;
    }

    public Set<MethodPos> getModifiedMethodsRef() {
        return modifiedMethodsRef;
    }

    public Set<MethodPos> getModifiedMethodsTgt() {
        return modifiedMethodsTgt;
    }

    public Set<MethodPos> getRemovedMethods() {
        return removedMethods;
    }

    public void prettyPrint() {
        System.out.println("------Added methods------:");
        addedMethods.forEach(System.out::println);
        System.out.println("------Removed methods-----:");
        removedMethods.forEach(System.out::println);
        System.out.println("------Modified methods in ref-----:");
        modifiedMethodsRef.forEach(System.out::println);
        System.out.println("------Modified methods in tgt-----:");
        modifiedMethodsTgt.forEach(System.out::println);
    }
}
