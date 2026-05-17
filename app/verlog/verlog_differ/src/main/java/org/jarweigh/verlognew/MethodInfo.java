package org.jarweigh.verlognew;

import java.util.Objects;

public class MethodInfo {
    private MethodPos methodPos;
    private String methodBody;

    public MethodInfo(MethodPos methodPos, String methodBody) {
        this.methodPos = methodPos;
        this.methodBody = methodBody;
    }

    public MethodPos getMethodPos() {
        return methodPos;
    }

    public void setMethodPos(MethodPos methodPos) {
        this.methodPos = methodPos;
    }

    public String getMethodBody() {
        return methodBody;
    }

    public void setMethodBody(String methodBody) {
        this.methodBody = methodBody;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodInfo)) return false;
        MethodInfo that = (MethodInfo) o;
        // If the method signature is the same, then the method is the same
        return methodPos.getSignature().equals(that.methodPos.getSignature());
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodPos.getSignature());
    }

    @Override
    public String toString() {
        return methodPos.toString();
    }
}
