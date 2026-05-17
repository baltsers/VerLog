package org.jarweigh.verlog.util;



import jas.Pair;
import org.jarweigh.verlog.data.AllReachableMethods;
import org.jarweigh.verlog.data.CallgraphPair;
import org.jarweigh.verlog.data.MethodDepthPair;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.HashMultiMap;
import soot.util.MultiMap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

public class GraphUtil {

    public static int MAX_DEPTH = 2;

    // Just use this ugly approach temporarily to get the reachable methods within depth = 2  for the given method
    public static MultiMap<String, String> getReachableMethodsDepth2(SootMethod startMethod) {
        MultiMap<String, String> reachableMethodsDepth2 = new HashMultiMap<>();
        //Depth 1
        Set<String> immediateSuccessors = getImmediateSuccessors(startMethod);
        //Depth 2
        try {
            for (String immediateSuccessor : immediateSuccessors) {
                if (MethodFilter.isLambdaMethod(immediateSuccessor)) {
                    //System.out.println("Skipping lambda method: " + immediateSuccessor);
                    continue;
                }
                Set<String> successors = getImmediateSuccessors(Scene.v().getMethod(immediateSuccessor));
                reachableMethodsDepth2.putAll(immediateSuccessor, successors);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            return reachableMethodsDepth2;
        }


        //return reachableMethodsDepth2;
    }



    public static Set<String> getImmediateSuccessors(SootMethod startMethod) {
        Set<String> immediateSuccessors = new HashSet<>();
        Stack<MethodDepthPair> stack = new Stack<>();

        // Push the starting method with depth 0
        stack.push(new MethodDepthPair(startMethod, 0));

        while (!stack.isEmpty()) {
            MethodDepthPair currentPair = stack.pop();
            SootMethod currentMethod = currentPair.method;
            int currentDepth = currentPair.depth;

            // Add the current method to the set of reachable methods, excluding entry point
            if (currentDepth > 0) {
                immediateSuccessors.add(currentMethod.getSignature());
                AllReachableMethods.getInstance().addMethod(currentMethod.getSignature());
            }

            // Proceed only if the current depth is less than maxDepth
            if (currentDepth < 1) {
                CallGraph cg = CallgraphPair.currentCallgraph;
                Iterator<Edge> edges = cg.edgesOutOf(currentMethod);

                while (edges.hasNext()) {
                    SootMethod targetMethod = edges.next().tgt();

                    // Push the target method with incremented depth
                    stack.push(new MethodDepthPair(targetMethod, currentDepth + 1));
                }
            }
        }
        return immediateSuccessors;

    }


    public static Set<String> findEntryPoints(Set<Pair<String, String>> edges){
        Set<String> allNodes = new HashSet<>();
        Set<String> destinationNodes = new HashSet<>();

        // Iterate through each edge and populate the sets
        for (Pair<String, String> edge : edges) {
            String source = edge.getO1();
            String destination = edge.getO2();

            allNodes.add(source);
            allNodes.add(destination);

            destinationNodes.add(destination);
        }

        // Find the difference between all nodes and destination nodes
        allNodes.removeAll(destinationNodes);

        // The remaining nodes in allNodes are those with no incoming edges
        return allNodes;
    }


}
