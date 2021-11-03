package com.izforge.izpack.util.sage;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallerException;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.SpecHelper;
import com.izforge.izpack.util.VariableSubstitutor;
import com.izforge.izpack.util.os.unix.ShellScript;

/**
 * X3-250275 Compile Prerequisite Control (on OL and RHEL) #367
 *
 */
public abstract class CAbstractOsInfosfinder {

	private final static String PREREQUISITES_SCRIPT = "PrerequisitesControlScript"; // "PrerequisitesControlScript.sh";
	protected static final String PLATFORM = OsVersion.IS_UNIX ? "unix":"windows";
	protected final CReport aReport;
	protected AutomatedInstallData aData;

	/**
	 * @param pReport
	 * @param pData
	 */
	public CAbstractOsInfosfinder(final CReport pReport, AutomatedInstallData pData) {
		super();
		this.aReport = pReport;
		this.aData = pData;
	}

	public String getResourceName() {
		return PREREQUISITES_SCRIPT;
	}


	/**
	 * @return
	 * @throws Exception
	 */
	public String execute() throws Exception {

		CLoggerUtils.logInfo("begin");
		String wShellScriptName = getResourceName();
		
		Debug.log("execute " + wShellScriptName);
		aReport.appendStep("Exec ScriptName=[%s]", wShellScriptName);

		String wOutput = loadPredefinedFunctions(getResourceName());
		aReport.appendOutput(wOutput);
		return wOutput;
	}



	private String loadPredefinedFunctions(String resource) throws Exception {
		String result = "";
		SpecHelper spechelper = new SpecHelper();
		String ext = OsVersion.IS_UNIX ? ".sh" : ".cmd";
		InputStream stream = spechelper.getResource(resource);
		if (stream != null) {
			InputStream substitutedStream = spechelper.substituteVariables(stream,
					new VariableSubstitutor(aData.getVariables()));

			File tempFile = File.createTempFile(resource, ext);
			FileOutputStream fos = null;
			tempFile.deleteOnExit();
			fos = new FileOutputStream(tempFile);

			aReport.append("ScriptLocation=[%s]", tempFile.getPath());

			byte[] buffer = new byte[1024];
			int len;
			while ((len = substitutedStream.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}

			substitutedStream.close();
			fos.flush();
			fos.close();

			// ok now we have to execute
			ProcessBuilder procBuilder = null;

			if (OsVersion.IS_UNIX) {
				procBuilder = new ProcessBuilder(System.getenv("SHELL"), tempFile.getAbsolutePath());
			} else {
				procBuilder = new ProcessBuilder("cmd.exe", "/C", tempFile.getAbsolutePath());
			}

			Debug.log("Launching " + tempFile.getAbsolutePath());
			Process p = procBuilder.start();
			InputStream errorOutput = new BufferedInputStream(p.getErrorStream(), 10000);
			InputStream consoleOutput = new BufferedInputStream(p.getInputStream(), 10000);

			Debug.log("ErrorOutput:");
			BufferedReader br = new BufferedReader(new InputStreamReader(errorOutput));
			String readErrorOutput = br.readLine();
			while (readErrorOutput != null) {
				Debug.log(readErrorOutput);
				readErrorOutput = br.readLine();
				result += readErrorOutput + "\r\n";
			}

			Debug.log("ConsoleOutput:");
			BufferedReader readerOutput = new BufferedReader(new InputStreamReader(consoleOutput));
			String readOutput = readerOutput.readLine();
			while (readOutput != null) {
				Debug.log(readOutput);
				readOutput = readerOutput.readLine();
				result += readOutput + "\r\n";
			}

			int exitCode = p.waitFor();

			Debug.log("ExitCode: " + exitCode);
			aReport.appendStep("exitCode: ", exitCode);

			if (exitCode != 0) {
				// script doesn't return 0 = SUCCESS
				// throw an exception
				// Debug.log("Command failed: "+ String.join(",", procBuilder.command()));
				Debug.log("Command failed: " + procBuilder.command());

				throw new InstallerException(resource + " return code is " + exitCode + " !");
			}
		}

		return result;
	}

}
