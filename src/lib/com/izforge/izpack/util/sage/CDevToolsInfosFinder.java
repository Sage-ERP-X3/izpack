package com.izforge.izpack.util.sage;

import com.izforge.izpack.installer.AutomatedInstallData;

/**
 * X3-250275 Compile Prerequisite Control (on OL and RHEL) #367
 * 
 * @author ogattaz
 *
 */
public class CDevToolsInfosFinder extends CAbstractOsInfosfinder {

	private final static String PREREQUISITES_SCRIPT = "PrerequisitesControlInfoScript"; // "PrerequisitesControlScript.sh";

	/**
	 * @param aReport
	 */
	public CDevToolsInfosFinder(final CReport pReport, AutomatedInstallData pData) {
		super(pReport, pData);
	}

	/*
	 * @override
	 */
	public String getResourceName() {
		return PREREQUISITES_SCRIPT + "_" + PLATFORM;
	}

	/**
	 * <pre>
	 * yum groupinfo "Development tools"
	 * </pre>
	 * 
	 * <pre>
			Last metadata expiration check: 0:07:22 ago on Tue 20 Jul 2021 08:25:24 AM EDT.
			
			Group: Development Tools
			Description: A basic development environment.
			Mandatory Packages:
			autoconf
			automake
			binutils
			bison
			flex
			gcc
			gcc-c++
			gdb
			glibc-devel
			libtool
			make
			pkgconf
			pkgconf-m4
			pkgconf-pkg-config
			redhat-rpm-config
			rpm-build
			rpm-sign
			strace
			Default Packages:
			asciidoc
			byacc
			ctags
			diffstat
			elfutils-libelf-devel
			git
			intltool
			jna
			ltrace
			patchutils
			perl-Fedora-VSP
			perl-Sys-Syslog
			perl-generators
			pesign
			source-highlight
			systemtap
			valgrind
			valgrind-devel
			Optional Packages:
			cmake
			dtrace
			expect
			rpmdevtools
			rpmlint
	 * </pre>
	 * 
	 * @see com.izforge.izpack.util.sage.CAbstractOsInfosfinder#getScriptLines()
	 */
	// Override
	/*
	 * public StringBuffer getScriptLines() { StringBuffer wLines = new
	 * StringBuffer();
	 * 
	 * addOneLine(wLines); addOneLine(wLines,
	 * "logStep \"Get the tools of the group [Development tools] using yum\"");
	 * addOneLine(wLines); addOneLine(wLines,
	 * "yum groupinfo \"Development tools\""); addOneLine(wLines,
	 * "if [ $? -ne 0 ]; then"); addOneLine(wLines,
	 * "  logErr \"Unable to get the list of tools \""); addOneLine(wLines,
	 * "  exit 1;"); addOneLine(wLines, "fi"); addOneLine(wLines);
	 * 
	 * return wLines; }
	 */

}
