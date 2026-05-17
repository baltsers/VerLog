package org.jarweigh.verlognew;

import java.util.ArrayList;
import java.util.Objects;

public class MethodPos {
    private String methodName;
    private String signature;
    private String className;
    private String returnType;
    private ArrayList<String> parameters;
    private int startLine;
    private int endLine;
    private boolean isAnonymous;

    public MethodPos(String methodName,
                     String signature,
                     String className,
                     String returnType,
                     ArrayList<String> parameters,
                     int startLine,
                     int endLine,
                     boolean isAnonymous) {
        this.methodName = methodName;
        this.signature = signature;
        this.className = className;
        this.returnType = returnType;
        this.parameters = parameters;
        this.startLine = startLine;
        this.endLine = endLine;
        this.isAnonymous = isAnonymous;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public String getSignature() {
        return this.signature;
    }

    public String getClassName() {
        return this.className;
    }

    public int getStartLine() {
        return this.startLine;
    }

    public int getEndLine() {
        return this.endLine;
    }

    public String getReturnType() {
        return this.returnType;
    }

    public ArrayList<String> getParameters() {
        return this.parameters;
    }

    public boolean isAnonymous() {
        return this.isAnonymous;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodPos that = (MethodPos) o;
        return signature.equals(that.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signature);
    }

    public String toString() {
        return String.format("Method: %s, Class: %s, Return Type: %s, Parameters: %s, Start Line: %d, End Line: %d",
                methodName, className, returnType, parameters, startLine, endLine);
    }
}
