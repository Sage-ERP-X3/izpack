package com.izforge.izpack.util;

import java.util.logging.Level;

import org.junit.Test;

import com.izforge.izpack.util.sage.CLoggerUtils;
import com.izforge.izpack.util.sage.CReport;
import com.izforge.izpack.util.sage.CWordList;
import com.izforge.izpack.util.sage.CWordList.EKindOfFinding;

/**
 * X3-250275 Compile Prerequisite Control (on OL and RHEL) #367
 * 
 * @author ogattaz
 *
 */
public class CTestWordList {

	/**
	 * 
	 */
	public CTestWordList() {
		super();
	}

	private String buildLines() {
		String wLines = "";
		wLines += "\n[root@localhost httpd-src]# yum list installed '*-devel*'";
		wLines += "\nInstalled Packages";
		wLines += "\napr-devel.x86_64            ";
		wLines += "\napr-util-devel.x86_64       ";
		wLines += "\nbinutils-devel.x86_64       ";
		wLines += "\ncyrus-sasl-devel.x86_64     ";
		wLines += "\nexpat-devel.x86_64          ";
		wLines += "\ngettext-common-devel.noarch ";
		wLines += "\ngettext-devel.x86_64        ";
		wLines += "\nglibc-devel.x86_64          ";
		wLines += "\nhttpd-devel.x86_64          ";
		wLines += "\nkernel-uek-devel.x86_64     ";
		wLines += "\nkernel-uek-devel.x86_64     ";
		wLines += "\nkernel-uek-devel.x86_64     ";
		wLines += "\nlibcurl-devel.x86_64        ";
		wLines += "\nlibdb-devel.x86_64          ";
		wLines += "\nlibstdc++-devel.x86_64      ";
		wLines += "\nlibxcrypt-devel.x86_64      ";
		wLines += "\nlibxml2-devel.x86_64        ";
		wLines += "\nopenldap-devel.x86_64       ";
		wLines += "\npcre-devel.x86_64           ";
		wLines += "\nperl-Devel-PPPort.x86_64    ";
		wLines += "\nperl-Devel-Peek.x86_64      ";
		wLines += "\nperl-Devel-SelfStubber.noarch";
		wLines += "\nperl-Devel-Size.x86_64      ";
		wLines += "\nperl-Encode-devel.x86_64    ";
		wLines += "\nperl-devel.x86_64           ";
		wLines += "\nsystemtap-devel.x86_64      ";
		wLines += "\nvalgrind-devel.x86_64       ";
		wLines += "\nxz-devel.x86_64             ";
		wLines += "\nzlib-devel.x86_64           ";
		wLines += "\n[root@localhost httpd-src]# yum list installed 'libxml2*'";
		wLines += "\nInstalled Packages";
		wLines += "\nlibxml2.x86_64 ";
		wLines += "\nlibxml2-devel.x86_64";
		return wLines;
	}

	/**
	 * 
	 */
	@Test
	public void testWordListA() {

		CLoggerUtils.logBanner(Level.INFO, "testWordListA : 6/6 success");

		CReport wReport = new CReport("testWordListA");

		String[] wLibraries = "pcre-devel.x86_64,apr-devel.x86_64,apr-util-devel.x86_64,httpd-devel,libxml2.x86_64,libxml2-devel.x86_64"
				.split(",");

		CWordList wWordList = new CWordList(wReport, "library", wLibraries);

		CLoggerUtils.logInfo("WordList= %s", wWordList);

		boolean wAllFound = wWordList.isAllWordsIn(buildLines(),
				EKindOfFinding.AT_THE_BEGINING_OF_A_LINE);

		CLoggerUtils.logInfo("AllFound=[%b]", wAllFound);

		CLoggerUtils.logInfo("Report:\n%s", wReport.toStringWithoutNow());
	}

	/**
	 * 
	 */
	@Test
	public void testWordListB() {

		CLoggerUtils.logBanner(Level.INFO, "testWordListB : 3/5 success");

		CReport wReport = new CReport("testWordListB");

		String[] wLibraries = "pcre-devel.x86_64,toto,httpd-devel,tutu,libxml2-devel.x86_64"
				.split(",");

		CWordList wWordList = new CWordList(wReport, "library", wLibraries);

		CLoggerUtils.logInfo("WordList= %s", wWordList);

		boolean wAllFound = wWordList.isAllWordsIn(buildLines(),
				EKindOfFinding.AT_THE_BEGINING_OF_A_LINE);

		CLoggerUtils.logInfo("AllFound=[%b]", wAllFound);

		CLoggerUtils.logInfo("Report:\n%s", wReport.toStringWithoutNow());
	}
}
