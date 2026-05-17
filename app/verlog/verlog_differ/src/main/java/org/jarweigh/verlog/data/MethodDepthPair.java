package org.jarweigh.verlog.data;

import soot.SootMethod;

public class MethodDepthPair {
    public SootMethod method;
    public int depth;

    public MethodDepthPair(SootMethod method, int depth) {
        this.method = method;
        this.depth = depth;
    }
}

