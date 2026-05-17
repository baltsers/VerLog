package org.jarweigh.verlognew;

import java.io.BufferedReader;
import java.util.HashSet;
import java.util.Set;
import java.io.StringReader;

public class GitRepoManager {

    private GitRepoManager() {
        throw new IllegalStateException("Utility class");
    }

    private static String gitDiffJavaFilesCmd(String refVersion, String tgtVersion) {
        return "git diff --name-status --diff-filter=AMD " + refVersion + ".." + tgtVersion + " "
                + "-- **/src/main/java/**/*.java";
    }

    /**
     * Get the changed classes,i.e., *.java files, between two versions of a git repository.
     * @param gitRepoPath
     * @param refVersion
     * @param tgtVersion
     * @return  ChangedClasses object containing the added, removed and modified classes.
     */
    public static ChangedClasses getChangedClasses(String gitRepoPath, String refVersion, String tgtVersion) {
        Set<String> addedClasses = new HashSet<>();
        Set<String> removedClasses = new HashSet<>();
        Set<String> modifiedClasses = new HashSet<>();

        String cmd = gitDiffJavaFilesCmd(refVersion, tgtVersion);
        String result = ShellManager.executeCommand(cmd, gitRepoPath);
        // The output is in the format:
        /*
        M       app/src/main/java/path/to/package/a.java
        A       app/src/main/java/path/to/package/b.java
         */
        try (BufferedReader reader = new BufferedReader(new StringReader(result))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("A")) {
                    addedClasses.add(line.substring(2));
                } else if (line.startsWith("D")) {
                    removedClasses.add(line.substring(2));
                } else if (line.startsWith("M")) {
                    modifiedClasses.add(line.substring(2));
                }
                System.out.println("Changed Java files:");
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ChangedClasses(addedClasses, removedClasses, modifiedClasses);
    }


}
