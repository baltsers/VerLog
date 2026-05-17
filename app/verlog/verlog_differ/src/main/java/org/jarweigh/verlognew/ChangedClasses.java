package org.jarweigh.verlognew;

import java.util.Set;

public class ChangedClasses {

    private Set<String> addedClasses;
    private Set<String> removedClasses;
    private Set<String> modifiedClasses;

    public ChangedClasses(Set<String> addedClasses, Set<String> removedClasses, Set<String> modifiedClasses) {
        this.addedClasses = addedClasses;
        this.removedClasses = removedClasses;
        this.modifiedClasses = modifiedClasses;
    }

    public Set<String> getAddedClasses() {
        return addedClasses;
    }

    public void setAddedClasses(Set<String> addedClasses) {
        this.addedClasses = addedClasses;
    }

    public Set<String> getRemovedClasses() {
        return removedClasses;
    }

    public void setRemovedClasses(Set<String> removedClasses) {
        this.removedClasses = removedClasses;
    }

    public Set<String> getModifiedClasses() {
        return modifiedClasses;
    }

    public void setModifiedClasses(Set<String> modifiedClasses) {
        this.modifiedClasses = modifiedClasses;
    }

    public void prettyPrint() {
        System.out.println("------Added classes------:");
        addedClasses.forEach(System.out::println);
        System.out.println("------Removed classes-----:");
        removedClasses.forEach(System.out::println);
        System.out.println("------Modified classes-----:");
        modifiedClasses.forEach(System.out::println);
    }

    @Override
    public String toString() {
        return "ChangedClasses{" +
                "addedClasses=" + addedClasses +
                ", removedClasses=" + removedClasses +
                ", modifiedClasses=" + modifiedClasses +
                '}';
    }
}
