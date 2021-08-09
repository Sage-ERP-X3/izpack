package com.izforge.izpack.util;

import java.io.File;

import org.junit.Test;

import com.izforge.izpack.util.os.unix.ShellScript;
import com.izforge.izpack.util.sage.CLoggerUtils;
import com.izforge.izpack.util.sage.CTextLineUtils;

/**
 * X3-250275 Compile Prerequisite Control (on OL and RHEL) #367
 * 
 * @author ogattaz
 *
 */
public class CTestShellScript {

	/**
	 * 
	 */
	public CTestShellScript() {
		super();
	}

	@Test
	public void testLs() {

		String wDashLine = CTextLineUtils.generateLineFull('-', 80);

		CLoggerUtils.logInfo("begin");

		// -------
		String wShellScriptName = "testLs.sh";

		CLoggerUtils.logInfo("ShellScriptName=[%s]", wShellScriptName);

		// -------
		String wDirPath = new File(System.getProperty("user.dir"))
				.getAbsolutePath();

		CLoggerUtils.logInfo("TempPath=[%s]", wDirPath);

		// -------
		String wShellScriptLocation = new File(wDirPath, wShellScriptName)
				.getAbsolutePath();

		CLoggerUtils.logInfo("Location=[%s]", wShellScriptLocation);

		// -------
		StringBuffer wLines = new StringBuffer();
		wLines.append('\n').append("echo ").append(wDashLine);
		wLines.append('\n').append("echo \"   nbParam=[${#}]\"");
		wLines.append('\n').append("echo \"    param1=[${1}]\"");
		wLines.append('\n').append("echo \"all params=[${@}]\"");
		wLines.append('\n').append("echo ").append(wDashLine);
		wLines.append('\n').append("pwd");
		wLines.append('\n').append("echo ").append(wDashLine);
		wLines.append('\n').append("ls -la .");
		wLines.append('\n').append("echo ").append(wDashLine);
		wLines.append('\n').append("echo \"nbfiles=[`ls -la . | wc -l`]\"");
		wLines.append('\n').append("echo ").append(wDashLine);

		CLoggerUtils.logInfo("ShellScript [%s]:\n%s", wShellScriptName,
				wLines.toString());

		// -------
		String wItsParams = "value01";

		CLoggerUtils.logInfo("ItsParams=[%s]", wItsParams);

		// -------
		String wOutput = ShellScript.execAndDelete(ShellScript.BASH, wLines,
				wShellScriptLocation, wItsParams);

		CLoggerUtils.logInfo("ShellScript output:\n%s", wOutput);

		CLoggerUtils.logInfo("end");

	}

}
