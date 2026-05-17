package org.jarweigh.verlognew;

import org.jarweigh.verlog.SootConfig;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileNotFoundException;


public class Main implements Runnable{

    @Option(names = {"--package-name"}, description = "Package name of the app", required = true)
    private String packageName;

    @Option(names = {"--repo"}, description = "Path to the code repository that has git history", required = true)
    private String gitRepoPath;

    @Option(names = {"--ref-version"}, description = "Reference version of the code repository", required = true)
    private String refVersion;

    @Option(names = {"--ref-dir"}, description = "Reference version", required = true)
    private String refVersionRepoPath;

    @Option(names = {"--ref-apk"}, description = "Path to the apk file of the reference version", required = true)
    private String refVersionApkPath;

    @Option(names = {"--tgt-version"}, description = "Target version", required = true)
    private String tgtVersion;

    @Option(names = {"--tgt-dir"}, description = "Target version of the code repository", required = true)
    private String tgtVersionRepoPath;

    @Option(names = {"--tgt-apk"}, description = "Path to the apk file of the target version", required = true)
    private String tgtVersionApkPath;

    @Option(names = {"--android-jar"}, description = "Path to the android jar file", required = true)
    private String androidJarPath;

    @Option(names = {"--output-dir"}, description = "Path to the output directory", required = true)
    private String outputDir;

    @Override
    public void run() {
        Globals.init(packageName, gitRepoPath, refVersion, refVersionRepoPath, refVersionApkPath, tgtVersion, tgtVersionRepoPath, tgtVersionApkPath, outputDir);
//        System.out.println("packageName: " + packageName);
//        System.out.println("gitRepoPath: " + gitRepoPath);
//        System.out.println("refVersion: " + refVersion);
//        System.out.println("refVersionRepoPath: " + refVersionRepoPath);
//        System.out.println("refVersionApkPath: " + refVersionApkPath);
//        System.out.println("tgtVersion: " + tgtVersion);
//        System.out.println("tgtVersionRepoPath: " + tgtVersionRepoPath);
//        System.out.println("tgtVersionApkPath: " + tgtVersionApkPath);
//        System.out.println("androidJarPath: " + androidJarPath);
//        System.out.println("outputDir: " + outputDir);
        try {
            if (androidJarPath != null) {
                SootConfig.androidJar = androidJarPath;
            }
            ContextualizedDiffer.run();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        
        CommandLine.run(new Main(), args);


        // System.out.println("Peak memory usage: " + peakMemoryUsage / (1024 * 1024) + " MB");
    }
}
