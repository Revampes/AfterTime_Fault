package com.aftertime.ratallofyou;

import java.io.*;
import java.nio.file.*;

public class VersionBumper {
    private static final String VERSION_FILE = "version.txt";

    public static void main(String[] args) {
        try {
            String commitMsg = getLatestCommitMessage();
            if (commitMsg == null) {
                System.out.println("No commit message found. Skipping version bump.");
                return;
            }
            int[] version = readVersion();
            int major = version[0];
            int minor = version[1];
            if (commitMsg.toLowerCase().startsWith("major")) {
                major += 1;
                minor = 0;
                writeVersion(major, minor);
                System.out.println("Bumped to major version: " + major + ".0");
            } else if (commitMsg.toLowerCase().startsWith("minor")) {
                minor += 1;
                writeVersion(major, minor);
                System.out.println("Bumped to minor version: " + major + "." + minor);
            } else {
                System.out.println("No version bump needed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getLatestCommitMessage() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("git", "log", "-1", "--pretty=%B");
        pb.redirectErrorStream(true);
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        process.waitFor();
        return sb.toString().trim();
    }

    private static int[] readVersion() throws IOException {
        String version = new String(Files.readAllBytes(Paths.get(VERSION_FILE))).trim();
        String[] parts = version.split("\\.");
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        return new int[]{major, minor};
    }

    private static void writeVersion(int major, int minor) throws IOException {
        Files.write(Paths.get(VERSION_FILE), (major + "." + minor + "\n").getBytes());
    }
}

