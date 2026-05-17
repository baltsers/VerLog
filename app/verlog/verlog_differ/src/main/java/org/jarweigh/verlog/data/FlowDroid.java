package org.jarweigh.verlog.data;

import org.jarweigh.verlog.SootConfig;
import org.jarweigh.verlog.util.SootUtil;
import soot.Scene;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.results.InfoflowPerformanceData;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.options.Options;

public class FlowDroid {


    public static void createCallGraph(String apkPath) {
        SootUtil.setupSoot(SootConfig.androidJar, apkPath);
        SetupApplication application = new SetupApplication(SootConfig.androidJar, apkPath);
        application.getConfig().setMergeDexFiles(true);
        Options.v().set_ignore_resolution_errors(true);
        Options.v().set_ignore_resolving_levels(true);
        Options.v().set_ignore_methodsource_error(true);
        application.getSootConfig().setSootOptions(Options.v(), application.getConfig());
        application.constructCallgraph();
    }
}
