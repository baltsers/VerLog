package org.jarweigh.verlog;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jarweigh.verlog.data.CallgraphPair;
import org.jarweigh.verlog.data.ChangedMethods;
import org.jarweigh.verlog.data.FlowDroid;
import org.jarweigh.verlog.data.MethodPos;
import org.jarweigh.verlog.service.ActivityClsService;
import org.jarweigh.verlog.service.GitMiningService;
import org.jarweigh.verlog.service.SourceCodeService;
import org.jarweigh.verlog.util.SerializationUtil;
import org.jarweigh.verlog.util.StringUtil;
import org.xmlpull.v1.XmlPullParserException;
import soot.Scene;
import soot.SootMethod;
import soot.util.MultiMap;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Main {


    public static void main(String[] args) throws IOException, XmlPullParserException {
        SootConfig.refAppPath = args[0];
        SootConfig.tgtAppPath = args[1];
        GitMiningService.REF_GIT_REPO_DIR = args[2];
        GitMiningService.TGT_GIT_REPO_DIR = args[3];
        // The app path should follow the format: /path/to/version_tag/app
        GitMiningService.REF_VERSION_TAG = StringUtil.getVersionTagFromAppPath(SootConfig.refAppPath);
        GitMiningService.TGT_VERSION_TAG = StringUtil.getVersionTagFromAppPath(SootConfig.tgtAppPath);
//        System.out.println(GitMiningService.REF_VERSION_TAG);
//        System.out.println(GitMiningService.TGT_VERSION_TAG);
//        System.out.println(GitMiningService.GIT_REPO_DIR);

        // Serialize all the changed methods
        JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
        ObjectNode rootNode = jsonNodeFactory.objectNode();

        System.out.println("Obtaining activity and fragment classes from the target and reference apps...");
        Set<String> tgtAppActClsStr = ActivityClsService.getActivityClasses(SootConfig.tgtAppPath);
        Set<String> refAppActClsStr = ActivityClsService.getActivityClasses(SootConfig.refAppPath);

        // intersection
        Set<String> sameActClsStr = new HashSet<>(tgtAppActClsStr); // Activity classes whose name are unchanged
        Set<String> addedActClsStr = new HashSet<>(tgtAppActClsStr); // new activity classes
        Set<String> removedActClsStr = new HashSet<>(refAppActClsStr); // removed activity classes
        sameActClsStr.retainAll(refAppActClsStr);
        addedActClsStr.removeAll(sameActClsStr);
        removedActClsStr.removeAll(sameActClsStr);

        System.out.println("Creating call graphs for the the reference app...");
        FlowDroid.createCallGraph(SootConfig.refAppPath);
        CallgraphPair.refCallGraph = Scene.v().getCallGraph();

        // Get all methods in same Activity class of reference app
        MultiMap<String, SootMethod> refMethodsInSameActCls = ActivityClsService.getClsWithMethods(sameActClsStr);

        // Get all methods in removed Activity class of reference app
        MultiMap<String, SootMethod> msInDeletedActs = ActivityClsService.getClsWithMethods(removedActClsStr);


        System.out.println("Creating call graphs for the the target app...");
        FlowDroid.createCallGraph(SootConfig.tgtAppPath);
        CallgraphPair.tgtCallGraph = Scene.v().getCallGraph();

        // Get all methods in same Activity class of target app
        MultiMap<String, SootMethod> tgtMethodsInSameActCls = ActivityClsService.getClsWithMethods(sameActClsStr);

        // Get all methods in added Activity class of target app
        MultiMap<String, SootMethod> msInAddedActs = ActivityClsService.getClsWithMethods(addedActClsStr);

        System.out.println("Comparing methods in the same activities...");
        // Get changed methods (cm) in same Activity class
        Map<String, ChangedMethods> cmsInModifiedActs = ActivityClsService.getDiffMethodsInSameCls(sameActClsStr,
                refMethodsInSameActCls, tgtMethodsInSameActCls);

        //cmsInModifiedActs.forEach((k, v) -> v.print());

        // Get the line number of the methods in the source code file of both versions
        MultiMap<String, MethodPos> MethodPosInDeletedCls = SourceCodeService.convertToMethodPos(msInDeletedActs.keySet(), GitMiningService.REF_VERSION_TAG);
//        for (String cls : cmsInModifiedActs.keySet()) {
//            System.out.println("Modified class " + cls);
//        }
        MultiMap<String, MethodPos> MethodPosInModifiedRefCls = SourceCodeService.convertToMethodPos(cmsInModifiedActs.keySet(), GitMiningService.REF_VERSION_TAG);
        MultiMap<String, MethodPos> MethodPosInAddedCls = SourceCodeService.convertToMethodPos(msInAddedActs.keySet(), GitMiningService.TGT_VERSION_TAG);
        MultiMap<String, MethodPos> MethodPosInModifiedTgtCls = SourceCodeService.convertToMethodPos(cmsInModifiedActs.keySet(), GitMiningService.TGT_VERSION_TAG);

        SerializationUtil.MethodPosInDeletedCls = MethodPosInDeletedCls;
        SerializationUtil.MethodPosInAddedCls = MethodPosInAddedCls;
        SerializationUtil.MethodPosInModifiedRefCls = MethodPosInModifiedRefCls;
        SerializationUtil.MethodPosInModifiedTgtCls = MethodPosInModifiedTgtCls;

        // release memory manually
        refMethodsInSameActCls = null;
        tgtMethodsInSameActCls = null;


        // Json Tree: depth 1 - Classes
        ArrayNode addClsArrNode = jsonNodeFactory.arrayNode();
        ArrayNode modClsArrNode = jsonNodeFactory.arrayNode();
        ArrayNode delClsArrNode = jsonNodeFactory.arrayNode();


        System.out.println("Serializing the results...");
        CallgraphPair.currentCallgraph = CallgraphPair.tgtCallGraph;
        SerializationUtil.serializeAddedActCls(msInAddedActs, addClsArrNode);
        SerializationUtil.serializeModifiedActCls(cmsInModifiedActs, modClsArrNode);
        CallgraphPair.currentCallgraph = CallgraphPair.refCallGraph;
        SerializationUtil.serializeDeletedActCls(msInDeletedActs, delClsArrNode);


        rootNode.set("added_activity_classes", addClsArrNode);
        rootNode.set("modified_activity_classes", modClsArrNode);
        rootNode.set("deleted_activity_classes", delClsArrNode);

        SerializationUtil.serializeJsonTree(rootNode);

        //System.out.println(rootNode.toPrettyString());


    }
}