package com.sage.izpack;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.helper.SpecHelper;

/**
 * X3-250275 Compile Prerequisite Control (on OL and RHEL) #367
 *
 */
public abstract class CAbstractOsInfosfinder {

	private static Logger logger = Logger.getLogger(CAbstractOsInfosfinder.class.getName());
	
	private final static String PREREQUISITES_SCRIPT = "PrerequisitesControlScript"; // "PrerequisitesControlScript.sh";
	protected static final String PLATFORM = OsVersion.IS_UNIX ? "unix":"windows";
	protected final CReport aReport;
	protected InstallData aData;
	protected Resources resources;

	/**
	 * @param pReport
	 * @param pData
	 */
	public CAbstractOsInfosfinder(final CReport pReport, InstallData pData, Resources resources) {
		super();
		this.aReport = pReport;
		this.aData = pData;
		this.resources = resources;
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
		
		logger.log(Level.FINE,  "execute " + wShellScriptName);
		aReport.appendStep("Exec ScriptName=[%s]", wShellScriptName);

		String wOutput = loadPredefinedFunctions(getResourceName(), this.resources);
		aReport.appendOutput(wOutput);
		return wOutput;
	}



	private String loadPredefinedFunctions(String resource, Resources resources) throws Exception {
		String result = "";
		SpecHelper spechelper = new SpecHelper(resources);
		String ext = OsVersion.IS_UNIX ? ".sh" : ".cmd";
		InputStream stream = spechelper.getResource(resource);
		if (stream != null) {
			
			// InputStream substitutedStream = spechelper.substituteVariables(stream, new VariableSubstitutor(aData.getVariables()));

			File tempFile = File.createTempFile(resource, ext);
			FileOutputStream fos = null;
			tempFile.deleteOnExit();
			fos = new FileOutputStream(tempFile);
			VariableSubstitutor substitutor = new VariableSubstitutorImpl(aData.getVariables());
			substitutor.substitute(stream, fos, SubstitutionType.TYPE_PLAIN, "UTF-8");

			aReport.append("ScriptLocation=[%s]", tempFile.getPath());

			/*
			byte[] buffer = new byte[1024];
			int len;
			while ((len = substitutedStream.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}

			substitutedStream.close();
			fos.flush();
			*/
			fos.close();

			// ok now we have to execute
			ProcessBuilder procBuilder = null;

			if (OsVersion.IS_UNIX) {
				procBuilder = new ProcessBuilder(System.getenv("SHELL"), tempFile.getAbsolutePath());
			} else {
				procBuilder = new ProcessBuilder("cmd.exe", "/C", tempFile.getAbsolutePath());
			}

			logger.log(Level.FINE, "Launching " + tempFile.getAbsolutePath());
			Process p = procBuilder.start();
			InputStream errorOutput = new BufferedInputStream(p.getErrorStream(), 10000);
			InputStream consoleOutput = new BufferedInputStream(p.getInputStream(), 10000);

			logger.log(Level.FINE, "ErrorOutput:");
			BufferedReader br = new BufferedReader(new InputStreamReader(errorOutput));
			String readErrorOutput = br.readLine();
			while (readErrorOutput != null) {
				logger.log(Level.FINE,  readErrorOutput);
				readErrorOutput = br.readLine();
				result += readErrorOutput + "\r\n";
			}

			logger.log(Level.FINE,  "ConsoleOutput:");
			BufferedReader readerOutput = new BufferedReader(new InputStreamReader(consoleOutput));
			String readOutput = readerOutput.readLine();
			while (readOutput != null) {
				logger.log(Level.FINE,  readOutput);
				readOutput = readerOutput.readLine();
				result += readOutput + "\r\n";
			}

			int exitCode = p.waitFor();
			logger.log(Level.FINE,  "ExitCode: " + exitCode);
			aReport.append("exitCode: ", exitCode);
			// aReport.appendStep("exitCode: ", exitCode);

			if (exitCode != 0) {
				// script doesn't return 0 = SUCCESS
				// throw an exception
				// Debug.log("Command failed: "+ String.join(",", procBuilder.command()));
				logger.log(Level.FINE,  "Command failed: " + procBuilder.command());

				throw new InstallerException(resource + " return code is " + exitCode + " !");
			}
		}

		return result;
	}

}
