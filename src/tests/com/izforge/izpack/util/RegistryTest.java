package com.izforge.izpack.util;

public class RegistryTest {

	public static void readRegistryTest() {
		String dotNetRelease = "HKLM:SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\v4\\Full:Release";
		String dotNetVersion = "HKLM:SOFTWARE\\Microsoft\\NET Framework Setup\\NDP\\v4\\Full:Version";
		String redistVisualCPP2017 = "HKLM:SOFTWARE\\Wow6432Node\\Microsoft\\VisualStudio\\14.0\\VC\\Runtimes\\x64:Major";

		String dotNetReleaseValue = IoHelper.getRegistry(dotNetRelease);
		String dotNetVersionValue = IoHelper.getRegistry(dotNetVersion);
		String redistCPP2017Value = IoHelper.getRegistry(redistVisualCPP2017);

		System.out.println(".net Release: " + dotNetReleaseValue);
		System.out.println(".net Version: " + dotNetVersionValue);
		System.out.println("Redist C++ 2017: " + redistCPP2017Value);
	}

}
