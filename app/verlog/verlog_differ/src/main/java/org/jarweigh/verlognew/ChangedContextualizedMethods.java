package org.jarweigh.verlognew;

import java.util.Set;

public class ChangedContextualizedMethods {
    private Set<ContextualizedMethodPos> addedContextualizedMethods;

    private Set<ContextualizedMethodPos> modifiedContextualizedMethodsRef;

    private Set<ContextualizedMethodPos> modifiedContextualizedMethodsTgt;
    private Set<ContextualizedMethodPos> removedContextualizedMethods;

    public ChangedContextualizedMethods() {
    }

    public ChangedContextualizedMethods(Set<ContextualizedMethodPos> addedContextualizedMethods, Set<ContextualizedMethodPos> modifiedContextualizedMethodsRef, Set<ContextualizedMethodPos> modifiedContextualizedMethodsTgt, Set<ContextualizedMethodPos> removedContextualizedMethods) {
        this.addedContextualizedMethods = addedContextualizedMethods;
        this.modifiedContextualizedMethodsRef = modifiedContextualizedMethodsRef;
        this.modifiedContextualizedMethodsTgt = modifiedContextualizedMethodsTgt;
        this.removedContextualizedMethods = removedContextualizedMethods;
    }

    public Set<ContextualizedMethodPos> getAddedContextualizedMethods() {
        return addedContextualizedMethods;
    }

    public void setAddedContextualizedMethods(Set<ContextualizedMethodPos> addedContextualizedMethods) {
        this.addedContextualizedMethods = addedContextualizedMethods;
    }

    public Set<ContextualizedMethodPos> getModifiedContextualizedMethodsRef() {
        return modifiedContextualizedMethodsRef;
    }

    public void setModifiedContextualizedMethodsRef(Set<ContextualizedMethodPos> modifiedContextualizedMethodsRef) {
        this.modifiedContextualizedMethodsRef = modifiedContextualizedMethodsRef;
    }

    public Set<ContextualizedMethodPos> getModifiedContextualizedMethodsTgt() {
        return modifiedContextualizedMethodsTgt;
    }

    public void setModifiedContextualizedMethodsTgt(Set<ContextualizedMethodPos> modifiedContextualizedMethodsTgt) {
        this.modifiedContextualizedMethodsTgt = modifiedContextualizedMethodsTgt;
    }

    public Set<ContextualizedMethodPos> getRemovedContextualizedMethods() {
        return removedContextualizedMethods;
    }

    public void setRemovedContextualizedMethods(Set<ContextualizedMethodPos> removedContextualizedMethods) {
        this.removedContextualizedMethods = removedContextualizedMethods;
    }
}
