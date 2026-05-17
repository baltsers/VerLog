package org.jarweigh.verlog.data;

import soot.SootMethod;

import java.util.Set;

public class ChangedMethods {
    private final Set<SootMethod> addedMethods;
    private final Set<SootMethod> modifiedMethodsRef;
    private final Set<SootMethod> modifiedMethodsTgt;
    private final Set<SootMethod> removedMethods;

    public ChangedMethods(Set<SootMethod> addedMethods,
                          Set<SootMethod> modifiedMethodsRef,
                          Set<SootMethod> modifiedMethodsTgt,
                          Set<SootMethod> removedMethods){
        this.addedMethods = addedMethods;
        this.removedMethods = removedMethods;
        this.modifiedMethodsRef = modifiedMethodsRef;
        this.modifiedMethodsTgt = modifiedMethodsTgt;
    }
    public Set<SootMethod> getAddedMethods() {
        return this.addedMethods;
    }

    public Set<SootMethod> getModifiedMethodsRef() {
        return this.modifiedMethodsRef;
    }

    public Set<SootMethod> getModifiedMethodsTgt() {
        return this.modifiedMethodsTgt;
    }

    public Set<SootMethod> getRemovedMethods() {
        return this.removedMethods;
    }

//    @Override
//    public boolean equals(Object obj) {
//        if (obj == this) return true;
//        if (!(obj instanceof ChangedMethods)) return false;
//        return addedMethods.equals(((ChangedMethods) obj).addedMethods) &&
//                modifiedMethods.equals(((ChangedMethods) obj).modifiedMethods) &&
//                removedMethods.equals(((ChangedMethods) obj).removedMethods);
//    }
    private void printOneCategoryMethods(Set<SootMethod> methods) {
        methods.stream().map(SootMethod::getSignature)
                .forEach(System.out::println);
    }

    public void print(){
        System.out.println("======Added methods======:");
        printOneCategoryMethods(this.addedMethods);
        System.out.println("======Modified methods======:");
        printOneCategoryMethods(this.modifiedMethodsRef);
        System.out.println("======Removed methods======:");
        printOneCategoryMethods(this.removedMethods);
    }

    public boolean isEmpty() {
        return this.addedMethods.isEmpty()
                && this.modifiedMethodsRef.isEmpty()
                && this.removedMethods.isEmpty();
    }

    @Override
    public String toString() {
        return "added methods: " + this.addedMethods +"\nmodified methods: " + this.modifiedMethodsRef
            + "\nremoved methods: " + this.removedMethods;
    }
}
