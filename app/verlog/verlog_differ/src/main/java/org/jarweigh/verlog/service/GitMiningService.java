package org.jarweigh.verlog.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;


public class GitMiningService {
    public static String REF_GIT_REPO_DIR;
    public static String TGT_GIT_REPO_DIR;
    public static String REF_VERSION_TAG;
    public static String TGT_VERSION_TAG;

    private GitMiningService() {
        throw new IllegalStateException("Utility class");
    }

    private static String getGitRepoDir(String versionTag) {
       if (versionTag.equals(REF_VERSION_TAG)) {
           return REF_GIT_REPO_DIR;
       } else if (versionTag.equals(TGT_VERSION_TAG)) {
           return TGT_GIT_REPO_DIR;
       } else {
           return null;
       }
    }

    /**
     * Get the source code file path of the class in the repository at the specified version tag.
     *
     * @param versionTag
     * @param className  the name of the class
     * @return the source code file path of the class
     */
    public static String getSrcCodePathOfClass(String versionTag, String className) {
        // Reset the repo to the version tag
//        try {
//            String[] cmd = {"/bin/sh", "-c", "cd " + GIT_REPO_DIR + " && git reset --soft " + versionTag};
//            Process p = Runtime.getRuntime().exec(cmd);
//            int exitValue = p.waitFor();
//            if (exitValue != 0) {
//                // If the exit value is not 0, the command did not terminate normally.
//                System.err.println("git reset command failed with exit value: " + exitValue);
//                return null; // Optionally return or handle the error as needed.
//            }
//
//        } catch (IOException | InterruptedException e) {
//            System.err.println("Error: " + e.getMessage());
//        }
        String gitRepoDir = getGitRepoDir(versionTag);
        // Get the source code content of the class
        try {
            String srcCodeFilePath = null;
            String[] cmd = {"/bin/sh", "-c", "cd " + gitRepoDir + " && find . -name " + className + ".java"};
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            srcCodeFilePath = reader.readLine();
            reader.close();
            return gitRepoDir + File.separator + srcCodeFilePath;
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
        // Return null if the source code file is not found
        return null;
    }
}