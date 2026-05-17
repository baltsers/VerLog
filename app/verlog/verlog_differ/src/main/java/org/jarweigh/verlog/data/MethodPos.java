package org.jarweigh.verlog.data;

import java.util.Objects;

public class MethodPos {
    private String methodName;
    private int startLine;
    private int endLine;

    public MethodPos(String methodName, int startLine, int endLine) {
        this.methodName = methodName;
        this.startLine = startLine;
        this.endLine = endLine;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public int getStartLine() {
        return this.startLine;
    }

    public int getEndLine() {
        return this.endLine;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof MethodPos)) return false;
        return methodName.equals(((MethodPos) obj).methodName) &&
                startLine == ((MethodPos) obj).startLine &&
                endLine == ((MethodPos) obj).endLine;
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodName, startLine, endLine);
    }

    @Override
    public String toString() {
        return String.format("Method: %s, Start Line: %d, End Line: %d",
                methodName, startLine, endLine);
    }
}