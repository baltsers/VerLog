package org.jarweigh.verlognew;

import org.json.simple.JSONObject;
import java.io.FileWriter;
import java.io.IOException;


public class PerformanceStat {
    public static double differencingTimeCost;
    public static int refCallGraphSize;
    public static double refCallGraphTimeCost;
    public static int tgtCallGraphSize;
    public static double tgtCallGraphTimeCost;
    public static double contextualizeTimeCost;
    public static double totalExecutionTime;
    //public static long peakMemoryUsage;


    public static double computeExecutionTimeInSeconds(long startTime, long endTime) {
        long durationInNano = endTime - startTime;
        // Convert nanoseconds to seconds
        return durationInNano / 1_000_000_000.0;
    }


    // save performance statistics to a json file
    public static void savePerformanceStat(String outputFilePath) {
        // save performance statistics to a json file
        try {
            JSONObject performanceStat = new JSONObject();
            performanceStat.put("differencingTimeCost", differencingTimeCost);
            performanceStat.put("refCallGraphSize", refCallGraphSize);
            performanceStat.put("refCallGraphTimeCost", refCallGraphTimeCost);
            performanceStat.put("tgtCallGraphSize", tgtCallGraphSize);
            performanceStat.put("tgtCallGraphTimeCost", tgtCallGraphTimeCost);
            performanceStat.put("contextualizeTimeCost", contextualizeTimeCost);
            performanceStat.put("totalExecutionTime", totalExecutionTime);
            //performanceStat.put("peakMemoryUsage",peakMemoryUsage / (1024 * 1024) + " MB");

            // write to file
            FileWriter file = new FileWriter(outputFilePath);
            file.write(performanceStat.toJSONString());
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}