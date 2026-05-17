package org.jarweigh.verlognew;

public class Globals {

    public static String packageName;

    public static String gitRepo;

    public static String refVersion;

    public static String refVersionRepo;

    public static String refVersionApkPath;

    public static String tgtVersion;

    public static String tgtVersionRepo;

    public static String tgtVersionApkPath;

    public static String outputDir;

    public static void init(String packageName,
                            String gitRepo,
                            String refVersion,
                            String refVersionRepo,
                            String refVersionApkPath,
                            String tgtVersion,
                            String tgtVersionRepo,
                            String tgtVersionApkPath,
                            String outputDir) {
        Globals.packageName = packageName;
        Globals.gitRepo = gitRepo;
        Globals.refVersion = refVersion;
        Globals.refVersionRepo = refVersionRepo;
        Globals.refVersionApkPath = refVersionApkPath;
        Globals.tgtVersion = tgtVersion;
        Globals.tgtVersionRepo = tgtVersionRepo;
        Globals.tgtVersionApkPath = tgtVersionApkPath;
        Globals.outputDir = outputDir;
    }

}
