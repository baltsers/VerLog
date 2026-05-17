package org.jarweigh.verlognew;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ShellManager {

    private ShellManager() {
        throw new IllegalStateException("Utility class");
    }

    public static String executeCommand(String cmd, String workingDir) {
        StringBuilder output = new StringBuilder();

        try {
            // System.out.println("Executing command: " + cmd);
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("sh", "-c", cmd);
            builder.directory(new File(workingDir));
            builder.redirectErrorStream(true);

            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Command execution failed with exit code: " + exitCode);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error executing command", e);
        }

        return output.toString();
    }
}
