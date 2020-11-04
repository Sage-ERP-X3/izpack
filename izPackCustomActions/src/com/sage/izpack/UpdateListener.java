package com.sage.izpack;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
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
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.core.substitutor.VariableSubstitutorInputStream;
import com.izforge.izpack.event.AbstractProgressInstallerListener;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.helper.SpecHelper;

// public class UpdateListener extends SimpleInstallerListener implements CleanupClient
public class UpdateListener extends AbstractProgressInstallerListener { //  implements com.izforge.izpack.util.CleanupClient {

	public UpdateListener(com.izforge.izpack.api.data.InstallData installData) {
		super(installData);
	}

	public static final String BEFORE_UPDATE_SCRIPT = "BeforeUpdateScript";
	public static final String BEFORE_INSTALL_SCRIPT = "BeforeInstallScript";
	public static final String AFTER_UPDATE_SCRIPT = "AfterUpdateScript";
	public static final String AFTER_INSTALL_SCRIPT = "AfterInstallScript";
	public static final String PLATFORM = OsVersion.IS_UNIX ? "unix" : "windows";

	private static final Logger logger = Logger.getLogger(AdxCompInstallerListener.class.getName());

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
	// public void afterPacks(AutomatedInstallData idata, AbstractUIProgressHandler
	// handler) throws Exception
	public void afterPacks(List<Pack> packs, ProgressListener listener) {
		// super.afterPacks(idata, handler);
		try {
			super.afterPacks(packs, listener);

			if (Boolean.valueOf(this.getInstallData().getVariable(InstallData.MODIFY_INSTALLATION))) {
				// at the top i imagine a first general action script
				// let says beforeUpdate Script

				fetchAndExcuteResource(AFTER_UPDATE_SCRIPT + "_" + PLATFORM, this.getInstallData());

				// we can call the update before/after script for each deleted packs
				// ???

			} else {

				fetchAndExcuteResource(AFTER_INSTALL_SCRIPT + "_" + PLATFORM, this.getInstallData());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
				fetchAndExcuteResource(pack.getLangPackId() + "_" + AFTER_UPDATE_SCRIPT + "_" + PLATFORM,
						getInstallData());
			} else {
				fetchAndExcuteResource(pack.getLangPackId() + "_" + AFTER_INSTALL_SCRIPT + "_" + PLATFORM,
						getInstallData());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
		// super.beforePacks(idata, npacks, handler);
		try {
			super.beforePacks(packs, listener);

			// if (Boolean.valueOf(idata.getVariable(InstallData.MODIFY_INSTALLATION)))
			if (Boolean.valueOf(getInstallData().getVariable(InstallData.MODIFY_INSTALLATION))) {
				// at the top i imagine a first general action script
				// let says beforeUpdate Script

				fetchAndExcuteResource(BEFORE_UPDATE_SCRIPT + "_" + PLATFORM, this.getInstallData());

				// we can call the update before/after script for each deleted packs
				// ???

			} else {
				fetchAndExcuteResource(BEFORE_INSTALL_SCRIPT + "_" + PLATFORM, this.getInstallData());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
		try { // super.beforePack(pack, i, handler);
			super.beforePack(pack);

			if (Boolean.valueOf(this.getInstallData().getVariable(InstallData.MODIFY_INSTALLATION))) {
				fetchAndExcuteResource(pack.getLangPackId() + "_" + BEFORE_UPDATE_SCRIPT + "_" + PLATFORM,
						getInstallData());
			} else {
				fetchAndExcuteResource(pack.getLangPackId() + "_" + BEFORE_INSTALL_SCRIPT + "_" + PLATFORM,
						getInstallData());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	//	super.afterInstallerInitialization(getInstallData());

		// do we need to initialize something ?
		// don't know for now but ??

	// }

	// public void fetchAndExcuteResource (String resource, AutomatedInstallData
	// idata)
	public void fetchAndExcuteResource(String resource, com.izforge.izpack.api.data.InstallData installData)
			throws Exception {
		
		SpecHelper spechelper = new SpecHelper(new ResourceManager());
		String ext = OsVersion.IS_UNIX ? ".sh" : ".cmd";
		InputStream stream = spechelper.getResource(resource);
		if (stream != null) {

			// InputStream substitutedStream = spechelper.substituteVariables(stream, new
			// VariableSubstitutor(installData.getVariables()));
			VariableSubstitutorInputStream substitutedStream = new VariableSubstitutorInputStream(stream,
					installData.getVariables(), SubstitutionType.TYPE_PLAIN, true);

			File tempFile = File.createTempFile(resource, ext);
			FileOutputStream fos = null;
			tempFile.deleteOnExit();
			fos = new FileOutputStream(tempFile);

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

			// Debug.log("launching "+tempFile.getAbsolutePath());
			Process p = procBuilder.start();
			InputStream errorOutput = new BufferedInputStream(p.getErrorStream(), 10000);
			InputStream consoleOutput = new BufferedInputStream(p.getInputStream(), 10000);

			// Debug.log("errorOutput:");

			BufferedReader br = new BufferedReader(new InputStreamReader(errorOutput));
			String read = br.readLine();
			while (read != null) {
				// Debug.log(read);
				logger.log(Level.FINE, read);
				read = br.readLine();
			}

			// Debug.log("consoleOutput:");

			BufferedReader br2 = new BufferedReader(new InputStreamReader(consoleOutput));
			String read2 = br2.readLine();
			while (read2 != null) {
				logger.log(Level.FINE, read2);
				// Debug.log(read2);
				read2 = br2.readLine();
			}

			int exitCode = p.waitFor();

			logger.log(Level.FINE, "exitCode: " + exitCode);
			// Debug.log("exitCode: "+ exitCode);

			if (exitCode != 0) {
				// script doesn't return 0 = SUCCESS
				// throw an exception
				// Debug.log("Command failed: "+ String.join(",", procBuilder.command()));
				logger.log(Level.FINE, "Command failed: " + procBuilder.command());
				// Debug.log("Command failed: " + procBuilder.command());

				throw new InstallerException(resource + " return code is " + exitCode + " !");
			}
		}

	}
}
