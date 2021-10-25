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
	 * @param aLines
	 * @return
	 */
	/*
	 * protected StringBuffer addOneLine(final StringBuffer aLines) {
	 * 
	 * return addOneLine(aLines, ""); }
	 */
	/**
	 * @param aLines
	 * @param aLine
	 * @return
	 */
	/*
	 * protected StringBuffer addOneLine(final StringBuffer aLines, final String
	 * aLine) {
	 * 
	 * aLines.append('\n').append(aLine);
	 * 
	 * return aLines; }
	 */

	/**
	 * @return
	 * @throws Exception
	 */
	public String execute() throws Exception {

		CLoggerUtils.logInfo("begin");
		String wShellScriptName = getResourceName();
		aReport.appendStep("Exec ScriptName=[%s]", wShellScriptName);

		// -------
		// File wScriptDir = getScriptDir();

		// pReport.append("ScriptDir=[%s]", wScriptDir.getAbsolutePath());
		// String wScriptLocation = new File(wScriptDir,
		// wShellScriptName).getAbsolutePath();
		// pReport.append("ScriptLocation=[%s]", wScriptLocation);
		// String wScriptParamter = getScriptParamter();
		// pReport.append("ScriptParamter=[%s]", wScriptParamter);
		// StringBuffer wLines = getScript();
		// dump the script in the report
		// pReport.appendScript(wLines.toString());
		// -------
		// String wOutput = ShellScript.execAndDelete(ShellScript.BASH, wLines,
		// wScriptLocation, wScriptParamter);

		String wOutput = loadPredefinedFunctions(getResourceName());
		aReport.appendOutput(wOutput);
		return wOutput;
	}

	/*
	 * public StringBuffer getScript() throws Exception {
	 * 
	 * StringBuffer wLines = new StringBuffer();
	 * 
	 * // wLines.append(loadPredefinedFunctions(PREREQUISITES_SCRIPT+"_"+PLATFORM));
	 * 
	 * wLines.append(getScriptLines());
	 * 
	 * addOneLine(wLines); addOneLine(wLines, "exit 0;"); addOneLine(wLines,
	 * "#eof");
	 * 
	 * return wLines;
	 * 
	 * }
	 */

	/**
	 * @return
	 */
	/*
	 * public File getScriptDir() {
	 * 
	 * return new File(System.getProperty("user.dir")); }
	 */

	// public abstract StringBuffer getScriptLines();

	/**
	 * @return
	 */
	// public String getScriptName() {
	// return String.format("%s.sh", getClass().getSimpleName());
	// }

	/**
	 * @return
	 */
	// public String getScriptParamter() {
	// return null;
	// }

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

			Debug.log("launching " + tempFile.getAbsolutePath());
			Process p = procBuilder.start();
			InputStream errorOutput = new BufferedInputStream(p.getErrorStream(), 10000);
			InputStream consoleOutput = new BufferedInputStream(p.getInputStream(), 10000);

			Debug.log("errorOutput:");

			BufferedReader br = new BufferedReader(new InputStreamReader(errorOutput));
			String read = br.readLine();
			while (read != null) {
				Debug.log(read);
				read = br.readLine();
				result += read;
			}

			Debug.log("consoleOutput:");

			BufferedReader br2 = new BufferedReader(new InputStreamReader(consoleOutput));
			String read2 = br2.readLine();
			while (read2 != null) {
				Debug.log(read2);
				read2 = br2.readLine();
				result += read;
			}

			int exitCode = p.waitFor();

			Debug.log("exitCode: " + exitCode);
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

	/**
	 * return throws Exception
	 *
	 * Finds a resource with a given name. The rules for searching resources
	 * associated with a given class are implemented by the defining class loader of
	 * the class. This method delegates to this object's class loader. If this
	 * object was loaded by the bootstrap class loader, the method delegates to
	 * ClassLoader.getSystemResource.
	 * 
	 * Before delegation, an absolute resource name is constructed from the given
	 * resource name using this algorithm:
	 * 
	 * If the name begins with a '/' ('\u002f'), then the absolute name of the
	 * resource is the portion of the name following the '/'. Otherwise, the
	 * absolute name is of the following form: modified_package_name/name Where the
	 * modified_package_name is the package name of this object with '/' substituted
	 * for '.' ('\u002e').
	 */
	/*
	 * protected String loadPredefinedFunctionsOLD() throws Exception { try {
	 * 
	 * return new String(Files.readAllBytes(
	 * Paths.get(getClass().getResource(PREREQUISITES_SCRIPT).toURI()))); } catch
	 * (Exception e) { throw new Exception(String.format(
	 * "Unable to load the resource [%s] from the package [%s]",
	 * PREREQUISITES_SCRIPT, this.getClass().getPackage().getName()), e); } }
	 */
}
