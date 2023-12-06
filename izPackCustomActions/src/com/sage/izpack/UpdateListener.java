package com.sage.izpack;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.event.AbstractProgressInstallerListener;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.helper.SpecHelper;

/*
    Execute UPDATE SCRIPTS
    <res id="BeforeUpdateScript_unix" src="updatescripts/beforepacks.sh" />
    <res id="BeforeUpdateScript_windows" src="updatescripts/beforepacks.cmd" />
    <res id="AfterUpdateScript_unix" src="updatescripts/afterpacks.sh" />
    <res id="AfterUpdateScript_windows" src="updatescripts/afterpacks.cmd" />

    <res id="base_BeforeUpdateScript_unix" src="updatescripts/beforebase.sh"/>
    <res id="base_BeforeUpdateScript_windows" src="updatescripts/beforebase.cmd"/>
    <res id="base_AfterUpdateScript_unix" src="updatescripts/afterbase.sh"/>
    <res id="base_AfterUpdateScript_windows" src="updatescripts/afterbase.cmd"/>
    <res id="productsSpec.txt" src="config/oldversions.txt"/>
    
	<res id="baseadxadminlinux64_BeforeUpdateScript_unix" src="updatescripts/beforepacks.sh" />
	<res id="baseadxadminwin64_BeforeUpdateScript_windows" src="updatescripts/beforepacks.cmd" />

  @author Franck DEPOORTERE
*/
public class UpdateListener extends AbstractProgressInstallerListener { // implements
																		// com.izforge.izpack.util.CleanupClient {

	/**
	 * The specification helper.
	 */
	private final SpecHelper spec;
	private com.izforge.izpack.api.resource.Resources resources;

	public UpdateListener(com.izforge.izpack.api.data.InstallData installData,
			com.izforge.izpack.api.resource.Resources resources) {
		super(installData);
		this.resources = resources;
		this.spec = new SpecHelper(resources);
	}

	public static final String BEFORE_UPDATE_SCRIPT = "BeforeUpdateScript";
	public static final String BEFORE_UPDATE_SCRIPT_PS = "BeforeUpdateScriptPs"; // PowerShell
	public static final String BEFORE_INSTALL_SCRIPT = "BeforeInstallScript";
	public static final String AFTER_UPDATE_SCRIPT = "AfterUpdateScript";
	public static final String AFTER_INSTALL_SCRIPT = "AfterInstallScript";
	public static final String PLATFORM = OsVersion.IS_UNIX ? "unix" : "windows";

	private static String prefixLabel = "UpdateListener.fetchAndExecuteResource ";

	private static final Logger logger = Logger.getLogger(UpdateListener.class.getName());

	public void cleanUp() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.izforge.izpack.event.SimpleInstallerListener#afterPacks(com.izforge.
	 * izpack.installer.AutomatedInstallData,
	 * com.izforge.izpack.util.AbstractUIProgressHandler)
	 */
	@Override
	public void afterPacks(List<Pack> packs, ProgressListener listener) {
		// super.afterPacks(idata, handler);
		try {
			super.afterPacks(packs, listener);

			if (Boolean.valueOf(this.getInstallData().getVariable(InstallData.MODIFY_INSTALLATION))) {
				// at the top i imagine a first general action script
				// let says beforeUpdate Script

				fetchAndExecuteResource(AFTER_UPDATE_SCRIPT + "_" + PLATFORM, null, this.getInstallData());

				// we can call the update before/after script for each deleted packs
				// ???

			} else {

				fetchAndExecuteResource(AFTER_INSTALL_SCRIPT + "_" + PLATFORM, null, this.getInstallData());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.izforge.izpack.event.SimpleInstallerListener#afterPack(com.izforge.izpack
	 * .Pack, java.lang.Integer, com.izforge.izpack.util.AbstractUIProgressHandler)
	 */
	@Override
	// public void afterPack(Pack pack, Integer i, AbstractUIProgressHandler
	// handler) throws Exception
	public void afterPack(Pack pack) throws InstallerException {
		try {
			// super.afterPack(pack, i, handler);
			super.afterPack(pack);

			if (Boolean.valueOf(getInstallData().getVariable(InstallData.MODIFY_INSTALLATION))) {
				// fetchAndExcuteResource(pack.id + "_" + AFTER_UPDATE_SCRIPT + "_" + PLATFORM,
				// getInstallData());
				fetchAndExecuteResource(pack.getLangPackId() + "_" + AFTER_UPDATE_SCRIPT + "_" + PLATFORM, null,
						getInstallData());
			} else {
				fetchAndExecuteResource(pack.getLangPackId() + "_" + AFTER_INSTALL_SCRIPT + "_" + PLATFORM, null,
						getInstallData());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InstallerException(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.izforge.izpack.event.SimpleInstallerListener#beforePacks(com.izforge.
	 * izpack.installer.AutomatedInstallData, java.lang.Integer,
	 * com.izforge.izpack.util.AbstractUIProgressHandler)
	 */
	@Override
	// public void beforePacks(AutomatedInstallData idata, Integer npacks,
	// AbstractUIProgressHandler handler) throws Exception
	public void beforePacks(List<Pack> packs, ProgressListener listener) {
		try {
			super.beforePacks(packs, listener);
			beforePacksCommon();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.izforge.izpack.event.SimpleInstallerListener#beforePack(com.izforge.
	 * izpack.Pack, java.lang.Integer,
	 * com.izforge.izpack.util.AbstractUIProgressHandler)
	 */
	@Override
	// public void beforePack(Pack pack, Integer i, AbstractUIProgressHandler
	// handler) throws Exception
	public void beforePack(Pack pack) {
		try {

			super.beforePack(pack);

			beforePacksCommon();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void beforePacksCommon() throws Exception {
		// if (Boolean.valueOf(idata.getVariable(InstallData.MODIFY_INSTALLATION)))
		if (Boolean.valueOf(getInstallData().getVariable(InstallData.MODIFY_INSTALLATION))) {
			// at the top i imagine a first general action script
			// let says beforeUpdate Script

			fetchAndExecuteResource(BEFORE_UPDATE_SCRIPT + "_" + PLATFORM, BEFORE_UPDATE_SCRIPT_PS + "_" + PLATFORM,
					this.getInstallData());

			// we can call the update before/after script for each deleted packs
			// ???

		} else {
			fetchAndExecuteResource(BEFORE_INSTALL_SCRIPT + "_" + PLATFORM, null, this.getInstallData());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.izforge.izpack.event.SimpleInstallerListener#afterInstallerInitialization
	 * (com.izforge.izpack.installer.AutomatedInstallData)
	 */
	// @Override
	// public void afterInstallerInitialization(AutomatedInstallData data)
	// public void afterInstallerInitialization() {
	// super.afterInstallerInitialization(getInstallData());

	// do we need to initialize something ?
	// don't know for now but ??

	// }

	// public void fetchAndExcuteResource (String resource, AutomatedInstallData
	// idata)
	public void fetchAndExecuteResource(String resource, String resourcePs,
			com.izforge.izpack.api.data.InstallData installData) throws Exception {

		logger.log(Level.FINE, prefixLabel + "( resource: " + resource + " resourcePs:" + resourcePs + ")");

		File tempFilePs = createTempFile(installData, resourcePs, resourcePs, ".ps1");
		if (tempFilePs != null) {
			installData.setVariable("BEFORE_UPDATE_SCRIPT_PS", tempFilePs.getName());
			installData.setVariable("BEFORE_UPDATE_SCRIPT_PS_PATH", tempFilePs.getPath());
			logger.log(Level.FINE,
					prefixLabel + "resourcePs:" + resourcePs + "  Temp file created: " + tempFilePs.getAbsolutePath()
							+ "  Add variable " + BEFORE_UPDATE_SCRIPT_PS + ":" + tempFilePs.getName());
		} else {
			logger.log(Level.FINE, prefixLabel + "NO resource found for resourcePs:" + resourcePs);
		}

		String ext = OsVersion.IS_UNIX ? ".sh" : ".cmd";
		// InputStream stream = spec.getResource(resource);
		File tempFile = createTempFile(installData, resource, resource, ext);
		if (tempFile != null) {

			/*
			 * VariableSubstitutor substitutor = new
			 * VariableSubstitutorImpl(installData.getVariables());
			 * substitutor.setBracesRequired(true); String result =
			 * substitutor.substitute(stream, SubstitutionType.TYPE_PLAIN);
			 * 
			 * File tempFile = File.createTempFile(resource, ext); FileOutputStream fos =
			 * new FileOutputStream(tempFile); tempFile.deleteOnExit();
			 * fos.write(result.getBytes()); fos.flush(); fos.close();
			 */
			logger.log(Level.FINE,
					prefixLabel + "resource:" + resource + "  Temp file created: " + tempFile.getAbsolutePath());

			// ok now we have to execute
			ProcessBuilder procBuilder = null;

			if (OsVersion.IS_UNIX) {
				procBuilder = new ProcessBuilder(System.getenv("SHELL"), tempFile.getAbsolutePath());
			} else {
				procBuilder = new ProcessBuilder("cmd.exe", "/C", tempFile.getAbsolutePath());
			}

			logger.log(Level.FINE, prefixLabel + "launching " + tempFile.getAbsolutePath());
			Process p = procBuilder.start();
			InputStream errorOutput = new BufferedInputStream(p.getErrorStream(), 10000);
			InputStream consoleOutput = new BufferedInputStream(p.getInputStream(), 10000);

			logger.log(Level.FINE, prefixLabel + "errorOutput:");

			BufferedReader brErrorOutput = new BufferedReader(new InputStreamReader(errorOutput));
			String readErrorOutput = brErrorOutput.readLine();
			while (readErrorOutput != null) {
				logger.log(Level.FINE, readErrorOutput);
				readErrorOutput = brErrorOutput.readLine();
			}

			logger.log(Level.FINE, prefixLabel + "consoleOutput:");

			BufferedReader brOutput = new BufferedReader(new InputStreamReader(consoleOutput));
			String read2 = brOutput.readLine();
			while (read2 != null) {
				logger.log(Level.FINE, read2);
				read2 = brOutput.readLine();
			}

			int exitCode = p.waitFor();

			logger.log(Level.FINE, prefixLabel + "ExitCode: " + exitCode);

			if (exitCode != 0) {
				// script doesn't return 0 = SUCCESS
				// throw an exception
				logger.log(Level.FINE, prefixLabel + "Command failed: " + procBuilder.command());
				throw new InstallerException(resource + " return code is " + exitCode + " !");
			}
		} else {
			logger.log(Level.FINE, prefixLabel + "NO resource found for resource:" + resource);
		}
	}

	private File createTempFile(InstallData installData, String resource, String fileName, String ext) {

		File tempFile = null;
		try {
			InputStream inputStream = this.spec.getResource(resource);
			if (inputStream == null && this.resources != null) {
				inputStream = this.resources.getInputStream(resource);
			}
			if (inputStream == null) {
				logger.log(Level.FINE,
						prefixLabel + "Cannot createTempFile(" + resource + ", " + fileName + ext + ") ");
				return null;
			}

			logger.log(Level.FINE, prefixLabel + "createTempFile(" + resource + ", " + fileName + ext + ") ");

			tempFile = File.createTempFile(fileName, ext);
			tempFile.deleteOnExit();
			FileOutputStream fos = new FileOutputStream(tempFile);

			VariableSubstitutor substitutor = new VariableSubstitutorImpl(installData.getVariables());
			substitutor.substitute(inputStream, fos, SubstitutionType.TYPE_PLAIN, "UTF-8");

			fos.close();
		} catch (com.izforge.izpack.api.exception.ResourceNotFoundException resNotFound) {
			logger.log(Level.FINE, prefixLabel + "Resource not found: " + resource + " " + resNotFound.getMessage());
			return null;
		} catch (com.izforge.izpack.api.exception.ResourceException resEx) {
			logger.log(Level.FINE, prefixLabel + "Resource error: " + resource + " " + resEx.getMessage());
			return null;
		} catch (IOException ex) {
			logger.log(Level.FINE, prefixLabel + "" + ex.getMessage());
			throw new InstallerException(
					prefixLabel + "I/O error during writing resource " + resource + " to a temporary buildfile", ex);
		} catch (Exception ex) {
			logger.log(Level.FINE, prefixLabel + "Resource gerror: " + resource + " " + ex.getMessage());
			return null;
		} finally {
		}

		return tempFile;
	}
}
