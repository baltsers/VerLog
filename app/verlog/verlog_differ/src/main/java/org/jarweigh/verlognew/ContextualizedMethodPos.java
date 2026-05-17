package org.jarweigh.verlognew;

import java.util.Set;

public class ContextualizedMethodPos {
    private MethodPos methodPos;
    private Set<String> reachableMethods;
    private String sootClassName;
    private String sootSignature;

    public ContextualizedMethodPos(MethodPos methodPos, Set<String> reachableMethods, String sootClassName, String sootSignature) {
        this.methodPos = methodPos;
        this.reachableMethods = reachableMethods;
        this.sootClassName = sootClassName;
        this.sootSignature = sootSignature;
    }

    public String getSootSignature() {
        return sootSignature;
    }

    public void setSootSignature(String sootSignature) {
        this.sootSignature = sootSignature;
    }

    public String getSootClassName() {
        return sootClassName;
    }

    public void setSootClassName(String sootClassName) {
        this.sootClassName = sootClassName;
    }

    public MethodPos getMethodPos() {
        return methodPos;
    }

    public void setMethodPos(MethodPos methodPos) {
        this.methodPos = methodPos;
    }

    public Set<String> getReachableMethods() {
        return reachableMethods;
    }

    public void setReachableMethods(Set<String> reachableMethods) {
        this.reachableMethods = reachableMethods;
    }
}
