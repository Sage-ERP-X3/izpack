package com.sage.izpack;

public class ArchitectureChecker {

    public static boolean is32Bit() {
    	return !is64Bit();
    }

    public static boolean is64Bit() {
        String osArch = System.getProperty("os.arch");

        // Check if the architecture contains "64" (case-insensitive)
        return osArch.toLowerCase().contains("64");
    }

}
