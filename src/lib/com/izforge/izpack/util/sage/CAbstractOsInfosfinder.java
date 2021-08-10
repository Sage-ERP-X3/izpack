package com.izforge.izpack.util.sage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.izforge.izpack.util.os.unix.ShellScript;

/**
 * X3-250275 Compile Prerequisite Control (on OL and RHEL) #367
 * 
 * @author ogattaz
 *
 */
public abstract class CAbstractOsInfosfinder {

	private final static String RESOURCE_NAME = "ShellScriptFunctions.sh";

	protected final CReport pReport;

	/**
	 * @param aReport
	 * @param aData
	 */
	public CAbstractOsInfosfinder(final CReport aReport) {
		super();
		pReport = aReport;
	}

	/**
	 * @param aLines
	 * @return
	 */
	protected StringBuffer addOneLine(final StringBuffer aLines) {

		return addOneLine(aLines, "");
	}

	/**
	 * @param aLines
	 * @param aLine
	 * @return
	 */
	protected StringBuffer addOneLine(final StringBuffer aLines,
			final String aLine) {

		aLines.append('\n').append(aLine);

		return aLines;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public String execute() throws Exception {

		CLoggerUtils.logInfo("begin");

		// -------
		String wShellScriptName = getScriptName();

		pReport.appendStep("Exec ScriptName=[%s]", wShellScriptName);

		// -------
		File wScriptDir = getScriptDir();

		pReport.append("ScriptDir=[%s]", wScriptDir.getAbsolutePath());

		// -------
		String wScriptLocation = new File(wScriptDir, wShellScriptName)
				.getAbsolutePath();

		pReport.append("ScriptLocation=[%s]", wScriptLocation);

		// -------
		String wScriptParamter = getScriptParamter();
		pReport.append("ScriptParamter=[%s]", wScriptParamter);

		// -------
		StringBuffer wLines = getScript();

		// dump the script in the report
		pReport.appendScript(wLines.toString());

		// -------
		String wOutput = ShellScript.execAndDelete(ShellScript.BASH, wLines,
				wScriptLocation, wScriptParamter);

		pReport.appendOutput(wOutput);

		return wOutput;
	}

	public StringBuffer getScript() throws Exception {

		StringBuffer wLines = new StringBuffer();

		wLines.append(loadPredefinedFunctions());

		wLines.append(getScriptLines());

		addOneLine(wLines);
		addOneLine(wLines, "exit 0;");
		addOneLine(wLines, "#eof");

		return wLines;

	}

	/**
	 * @return
	 */
	public File getScriptDir() {

		return new File(System.getProperty("user.dir"));
	}

	/**
	 * @return
	 */
	public abstract StringBuffer getScriptLines();

	/**
	 * @return
	 */
	public String getScriptName() {
		return String.format("%s.sh", getClass().getSimpleName());
	}

	/**
	 * @return
	 */
	public String getScriptParamter() {
		return null;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	protected String loadPredefinedFunctions() throws Exception {
		try {
			/**
			 * Finds a resource with a given name. The rules for searching
			 * resources associated with a given class are implemented by the
			 * defining class loader of the class. This method delegates to this
			 * object's class loader. If this object was loaded by the bootstrap
			 * class loader, the method delegates to
			 * ClassLoader.getSystemResource.
			 * 
			 * Before delegation, an absolute resource name is constructed from
			 * the given resource name using this algorithm:
			 * 
			 * If the name begins with a '/' ('\u002f'), then the absolute name
			 * of the resource is the portion of the name following the '/'.
			 * Otherwise, the absolute name is of the following form:
			 * modified_package_name/name Where the modified_package_name is the
			 * package name of this object with '/' substituted for '.'
			 * ('\u002e').
			 */
			return new String(Files.readAllBytes(
					Paths.get(getClass().getResource(RESOURCE_NAME).toURI())));
		} catch (Exception e) {
			throw new Exception(String.format(
					"Unable to load the ressource [%s] from the package [%s]",
					RESOURCE_NAME, this.getClass().getPackage().getName()), e);
		}
	}
}
