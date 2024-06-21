package com.sage.izpack;

public class ArchitectureChecker {

    public static boolean is32Bit() {
    	return !is64Bit();
    }

    public static boolean is64Bit() {
        String osArch = System.getProperty("os.arch");
        return osArch.contains("64");
    }

}
