package org.jarweigh.verlog.util;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.jarweigh.verlog.SootConfig;
import org.jarweigh.verlog.data.*;
import org.xmlpull.v1.XmlPullParserException;
import soot.SootMethod;
import soot.util.MultiMap;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;


public class SerializationUtil {
    private static JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;

    public static MultiMap<String, MethodPos> MethodPosInDeletedCls;
    public static MultiMap<String, MethodPos> MethodPosInAddedCls;
    public static MultiMap<String, MethodPos> MethodPosInModifiedRefCls;
    public static MultiMap<String, MethodPos> MethodPosInModifiedTgtCls;


    public static void serializeJsonTree(JsonNode rootNode) throws IOException, XmlPullParserException {
        String packageName = AndroidUtil.getPackageName(SootConfig.tgtAppPath);

        JsonFactory jsonFactory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper();

        try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(
                new File(SootConfig.USER_DIR + "/out/"+packageName+"/", packageName + "-diff.json"),
                JsonEncoding.UTF8)) {
            mapper.writeTree(jsonGenerator, rootNode);
        }
    }

    // Convert the reachable methods of changed methods in modified activity classes to Json tree
    public static void serializeModifiedActCls(Map<String, ChangedMethods> cmsInModCls,
                                               ArrayNode modClsArrNode) {
        //cmsInModifiedActs.
        for (String cls : cmsInModCls.keySet()) {

            CallgraphPair.currentCallgraph = CallgraphPair.tgtCallGraph;

            ObjectNode clsNcmObjNode = jsonNodeFactory.objectNode();
            clsNcmObjNode.set("class_name", TextNode.valueOf(cls));

            Set<SootMethod> amInModCls = cmsInModCls.get(cls).getAddedMethods();
            ArrayNode amNrmInModClsArrNode = jsonNodeFactory.arrayNode();
            buildCmWithRmArrNode(amNrmInModClsArrNode, cls, amInModCls, MethodChangeType.ADDED_METHOD_IN_MODIFIED_CLASS);
            clsNcmObjNode.set(MethodChangeType.ADDED_METHOD_IN_MODIFIED_CLASS.name(), amNrmInModClsArrNode);

            Set<SootMethod> mmInModClsTgt = cmsInModCls.get(cls).getModifiedMethodsTgt();
            ArrayNode mmNrmInModTgtClsArrNode = jsonNodeFactory.arrayNode();
            buildCmWithRmArrNode(mmNrmInModTgtClsArrNode, cls, mmInModClsTgt, MethodChangeType.MODIFIED_METHOD_IN_TGT_CLASS);
            clsNcmObjNode.set(MethodChangeType.MODIFIED_METHOD_IN_TGT_CLASS.name(), mmNrmInModTgtClsArrNode);

            CallgraphPair.currentCallgraph = CallgraphPair.refCallGraph;

            Set<SootMethod> mmInModClsRef = cmsInModCls.get(cls).getModifiedMethodsRef();
            ArrayNode mmNrmInModRefClsArrNode = jsonNodeFactory.arrayNode();
            buildCmWithRmArrNode(mmNrmInModRefClsArrNode, cls, mmInModClsRef, MethodChangeType.MODIFIED_METHOD_IN_REF_CLASS);
            clsNcmObjNode.set(MethodChangeType.MODIFIED_METHOD_IN_REF_CLASS.name(), mmNrmInModRefClsArrNode);

            Set<SootMethod> dmInModCls = cmsInModCls.get(cls).getRemovedMethods();
            ArrayNode dmNrmInModClsArrNode = jsonNodeFactory.arrayNode();
            buildCmWithRmArrNode(dmNrmInModClsArrNode, cls, dmInModCls, MethodChangeType.DELETED_METHOD_IN_MODIFIED_CLASS);
            clsNcmObjNode.set(MethodChangeType.DELETED_METHOD_IN_MODIFIED_CLASS.name(), dmNrmInModClsArrNode);

            //lsNcmObjNode.set()
            modClsArrNode.add(clsNcmObjNode);
        }
    }



    public static void serializeAddedActCls(MultiMap<String, SootMethod> msInAddedActs,
                                            ArrayNode addClsArrNode) {
        buildcClassArrNode(addClsArrNode, msInAddedActs, MethodChangeType.ADDED_METHOD_IN_ADDED_CLASS);
    }

    public static void serializeDeletedActCls(MultiMap<String, SootMethod> msInDeletedActs,
                                              ArrayNode delClsArrNode) {
        buildcClassArrNode(delClsArrNode, msInDeletedActs, MethodChangeType.DELETED_METHOD_IN_DELETED_CLASS);
    }


    /**
     * build [] in {"added_class":[]}
     * @param cClzArrNode {"added_class":(changed classes array node)}
     * @param clzWithCMethods classes with their changed methods
     * @param changeType added/modified/deleted
     */
    private static void buildcClassArrNode(ArrayNode cClzArrNode,
                                           MultiMap<String, SootMethod> clzWithCMethods,
                                           MethodChangeType changeType) {
        for (String cls : clzWithCMethods.keySet()) {
            ObjectNode clsNcmObjNode = jsonNodeFactory.objectNode();
            clsNcmObjNode.set("class_name", TextNode.valueOf(cls));
            Set<SootMethod> cmInCls = clzWithCMethods.get(cls);
            ArrayNode cmNrmArrNode = jsonNodeFactory.arrayNode();
            buildCmWithRmArrNode(cmNrmArrNode, cls, cmInCls, changeType);
            clsNcmObjNode.set(changeType.name(), cmNrmArrNode);
            cClzArrNode.add(clsNcmObjNode);
        }

    }

    /**
     * build [] in {}
     * @param cmNrmArrNode [] contain {"method_name": name, "reachable_method": [...]}
     * @param changedMethods
     */
    private static void buildCmWithRmArrNode(ArrayNode cmNrmArrNode,
                                             String classSignature,
                                             Set<SootMethod> changedMethods,
                                             MethodChangeType changeType) {
        for (SootMethod method : changedMethods) {
            // skip <init> and <clinit>
            if (MethodFilter.isInitMethod(method.getSignature())) {
                continue;
            }
            if (MethodFilter.isAccessMethod(method.getSignature())) {
                continue;
            }
            if (MethodFilter.isLambdaMethod(method.getSignature())) {
                continue;
            }
            // A lightweight solution to avoid overlapped reachable methods
            // e.g. m1 is modified, m2 is added, and m1 calls m2, then m2 will be in the reachable methods of m1
            // therefore, no need to find reachable methods of m2
//            if (AllReachableMethods.getInstance().isInAllReachableMethods(method.getSignature())) {
//                continue;
//            }


            ObjectNode cmWithRmsNode = jsonNodeFactory.objectNode();
            cmWithRmsNode.set("method_name", TextNode.valueOf(method.getSignature()));

            switch (changeType)
            {
                case ADDED_METHOD_IN_ADDED_CLASS:
                    cmWithRmsNode.set("line_number", TextNode.valueOf(getLineNumFromMultimap(MethodPosInAddedCls, classSignature, method.getSignature())));
                    break;
                case DELETED_METHOD_IN_MODIFIED_CLASS:
                case MODIFIED_METHOD_IN_REF_CLASS:
                    cmWithRmsNode.set("line_number", TextNode.valueOf(getLineNumFromMultimap(MethodPosInModifiedRefCls, classSignature, method.getSignature())));
                    break;
                case ADDED_METHOD_IN_MODIFIED_CLASS:
                case MODIFIED_METHOD_IN_TGT_CLASS:
                    cmWithRmsNode.set("line_number", TextNode.valueOf(getLineNumFromMultimap(MethodPosInModifiedTgtCls, classSignature, method.getSignature())));
                    break;
                case DELETED_METHOD_IN_DELETED_CLASS:
                    cmWithRmsNode.set("line_number", TextNode.valueOf(getLineNumFromMultimap(MethodPosInDeletedCls, classSignature, method.getSignature())));
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected value: " + changeType);
            }

            ArrayNode rmsNode = jsonNodeFactory.arrayNode();
            buildRmArrNodeDepth1(rmsNode, method);
            cmWithRmsNode.set("reachable_methods", rmsNode);
            cmNrmArrNode.add(cmWithRmsNode);
        }
    }

    private static String getLineNumFromMultimap(MultiMap<String, MethodPos> methodPosMap, String classSignature, String methodSignature) {
        MethodPos matchedMethodPos = null;
        for (String cls : methodPosMap.keySet()) {
            if (cls.equals(classSignature)) {
                for (MethodPos methodPos : methodPosMap.get(cls)) {
                    //System.out.println(convertSootMethodSignatureToJavaParserSignature(methodSignature));
                    if (methodPos.getMethodName().equals(StringUtil.convertSootMethodSignatureToJavaParserSignature(methodSignature))) {
                        System.out.println(methodPos.getMethodName()+":  "+StringUtil.convertSootMethodSignatureToJavaParserSignature(methodSignature));
                        matchedMethodPos = methodPos;
                    }
                }
            }
        }
        if (matchedMethodPos != null) {
            return matchedMethodPos.getStartLine()+"-"+matchedMethodPos.getEndLine();
        }
        return "0-0";
    }

    /**
     * build node [{"method_name":name, "reachable_method": [reachable_method_1, reachable_method_2, ...]}, {"method_name":name}...]
     * @param rmArrNode reachable methods array node
     * @param startMethod
     */
    private static void buildRmArrNodeDepth2(ArrayNode rmArrNode, SootMethod startMethod) {
        MultiMap<String, String> reachableMethods = GraphUtil.getReachableMethodsDepth2(startMethod);
        for (String method : reachableMethods.keySet()) {
            ObjectNode rmDepth1Node = jsonNodeFactory.objectNode();
            rmDepth1Node.set("method_name", TextNode.valueOf(method));
            //ArrayNode rmsNode = jsonNodeFactory.arrayNode();
            if (reachableMethods.get(method).isEmpty()) {
                rmArrNode.add(rmDepth1Node);
                continue;
            }
            ArrayNode rmDepth2Node = jsonNodeFactory.arrayNode();
            reachableMethods.get(method).stream()
                            .filter(rm -> !MethodFilter.isInitMethod(rm))
                            //.filter(rm -> !MethodFilter.isKotlinResultKitMethod(rm))
                            .filter(rm -> !MethodFilter.isAccessMethod(rm))
                            .forEach(rmDepth2Node::add);
            rmDepth1Node.set("reachable_methods", rmDepth2Node);
            rmArrNode.add(rmDepth1Node);
        }


    }

    private static void buildRmArrNodeDepth1(ArrayNode rmArrNode,
                                             SootMethod startMethod) {
        Set<String> immediateSuccessors = GraphUtil.getImmediateSuccessors(startMethod);
        immediateSuccessors.stream()
                .filter(rm -> !MethodFilter.isInitMethod(rm))
                .filter(rm -> !MethodFilter.isAccessMethod(rm))
                .forEach(rmArrNode::add);
    }

}
