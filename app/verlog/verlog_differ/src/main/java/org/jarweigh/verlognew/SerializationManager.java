package org.jarweigh.verlognew;

import com.fasterxml.jackson.databind.node.ArrayNode;
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
import org.jarweigh.verlog.data.CallgraphPair;
import org.jarweigh.verlog.data.MethodChangeType;
import org.jarweigh.verlog.util.AndroidUtil;
import org.jarweigh.verlog.util.SerializationUtil;
import org.xmlpull.v1.XmlPullParserException;
import soot.util.MultiMap;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class SerializationManager {
        private static JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;

        private SerializationManager() {
            throw new IllegalStateException("Utility class");
        }

        public static void serializeAllChangedMethods(MultiMap<String, ContextualizedMethodPos> addedClsMethods,
                                                      MultiMap<String, ContextualizedMethodPos> removedClsMethods,
                                                      Map<String, ChangedContextualizedMethods> modifiedClsMethods) {

            ObjectNode rootNode = jsonNodeFactory.objectNode();
            ArrayNode addClsArrNode = jsonNodeFactory.arrayNode();
            ArrayNode modClsArrNode = jsonNodeFactory.arrayNode();
            ArrayNode delClsArrNode = jsonNodeFactory.arrayNode();

            System.out.println("Serializing the results to" + Globals.outputDir + "...");
            serializeAddedCls(addedClsMethods, addClsArrNode);
            serializeModifiedCls(modifiedClsMethods, modClsArrNode);
            serializeDeletedCls(removedClsMethods, delClsArrNode);

            rootNode.set("added_classes", addClsArrNode);
            rootNode.set("modified_classes", modClsArrNode);
            rootNode.set("deleted_classes", delClsArrNode);

            try {
                SerializeJsonNode(rootNode, Globals.outputDir);
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }

        }

        private static void SerializeJsonNode(ObjectNode rootNode, String outputDirPath) throws XmlPullParserException, IOException {
            String packageName = Globals.packageName;

            JsonFactory jsonFactory = new JsonFactory();
            ObjectMapper mapper = new ObjectMapper();

            try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(
                    new File(outputDirPath+"/", packageName + "-" + Globals.refVersion
                            + "-" + Globals.tgtVersion + "-diff.json"),
                    JsonEncoding.UTF8)) {
                mapper.writeTree(jsonGenerator, rootNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private static void serializeModifiedCls(Map<String, ChangedContextualizedMethods> modifiedClsMethods, ArrayNode modClsArrNode) {
            for (String modifiedCls : modifiedClsMethods.keySet()) {
                ObjectNode modifiedClsNode = jsonNodeFactory.objectNode();
                modifiedClsNode.set("class_name", TextNode.valueOf(modifiedCls));
                ChangedContextualizedMethods changedMethods = modifiedClsMethods.get(modifiedCls);
                ArrayNode addedMethodsArrNode = jsonNodeFactory.arrayNode();
                for (ContextualizedMethodPos addedMethod : changedMethods.getAddedContextualizedMethods()) {
                    buildMethodObjNode(addedMethodsArrNode, addedMethod);
                }
                modifiedClsNode.set(MethodChangeType.ADDED_METHOD_IN_MODIFIED_CLASS.name(), addedMethodsArrNode);
                ArrayNode modifiedRefMethodsArrNode = jsonNodeFactory.arrayNode();
                for (ContextualizedMethodPos modifiedMethodRef : changedMethods.getModifiedContextualizedMethodsRef()) {
                    buildMethodObjNode(modifiedRefMethodsArrNode, modifiedMethodRef);
                }
                modifiedClsNode.set(MethodChangeType.MODIFIED_METHOD_IN_REF_CLASS.name(), modifiedRefMethodsArrNode);
                ArrayNode modifiedTgtMethodsArrNode = jsonNodeFactory.arrayNode();
                for (ContextualizedMethodPos modifiedMethodTgt : changedMethods.getModifiedContextualizedMethodsTgt()) {
                    buildMethodObjNode(modifiedTgtMethodsArrNode, modifiedMethodTgt);
                }
                modifiedClsNode.set(MethodChangeType.MODIFIED_METHOD_IN_TGT_CLASS.name(), modifiedTgtMethodsArrNode);
                ArrayNode removedMethodsArrNode = jsonNodeFactory.arrayNode();
                for (ContextualizedMethodPos removedMethod : changedMethods.getRemovedContextualizedMethods()) {
                    buildMethodObjNode(removedMethodsArrNode, removedMethod);
                }
                modifiedClsNode.set(MethodChangeType.DELETED_METHOD_IN_MODIFIED_CLASS.name(), removedMethodsArrNode);
                modClsArrNode.add(modifiedClsNode);
            }
        }

    private static void buildMethodObjNode(ArrayNode MethodsArrNode, ContextualizedMethodPos Method) {
        ObjectNode addedMethodNode = jsonNodeFactory.objectNode();
        addedMethodNode.set("method_name", TextNode.valueOf(Method.getSootSignature()));
        addedMethodNode.set("line_number", TextNode.valueOf(Method.getMethodPos().getStartLine() + "-"
                + Method.getMethodPos().getEndLine()));
        Set<String> reachableMethods = Method.getReachableMethods();
        ArrayNode reachableMethodsArrNode = jsonNodeFactory.arrayNode();
        for (String reachableMethod : reachableMethods) {
            reachableMethodsArrNode.add(TextNode.valueOf(reachableMethod));
        }
        addedMethodNode.set("reachable_methods", reachableMethodsArrNode);
        MethodsArrNode.add(addedMethodNode);
    }

    private static void serializeAddedCls(MultiMap<String, ContextualizedMethodPos> addedClsMethods, ArrayNode addClsArrNode) {
            serializeAddedOrRemovedCls(addedClsMethods, addClsArrNode, MethodChangeType.ADDED_METHOD_IN_ADDED_CLASS);
        }

        private static void serializeDeletedCls(MultiMap<String, ContextualizedMethodPos> removedClsMethods, ArrayNode delClsArrNode) {
            serializeAddedOrRemovedCls(removedClsMethods, delClsArrNode, MethodChangeType.DELETED_METHOD_IN_DELETED_CLASS);
        }

        private static void serializeAddedOrRemovedCls(MultiMap<String, ContextualizedMethodPos> addedClsMethods, ArrayNode addClsArrNode, MethodChangeType changeType) {
            for (String addedCls : addedClsMethods.keySet()) {
                ObjectNode addedClsNode = jsonNodeFactory.objectNode();
                addedClsNode.set("class_name", TextNode.valueOf(addedCls));
                ArrayNode methodsArrNode = jsonNodeFactory.arrayNode();
                for (ContextualizedMethodPos method : addedClsMethods.get(addedCls)) {
                    buildMethodObjNode(methodsArrNode, method);
                }
                addedClsNode.set(changeType.name(), methodsArrNode);
                addClsArrNode.add(addedClsNode);
            }
        }
}
