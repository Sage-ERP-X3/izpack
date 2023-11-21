package com.sage.izpack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.panels.finish.FinishPanelAutomation;
import com.izforge.izpack.util.OsVersion;

public class FinishNewPanelAutomationHelper extends FinishPanelAutomation {

	private static Logger logger = Logger.getLogger(FinishNewPanelAutomationHelper.class.getName());

	private ResourcesHelper resourceHelper;
	private final InstallData installData;
	private final UninstallDataWriter uninstallDataWriter;
	private final UninstallData uninstallData;
	private static String logPrefix = "FinishNewPanelAutomationHelper instance. ";
	private final Resources resources;

	public FinishNewPanelAutomationHelper(InstallData installData, Resources resources,
			UninstallDataWriter uninstallDataWriter, UninstallData uninstallData) throws NativeLibException {
		super();

		logger.log(Level.FINE, logPrefix + "Init custom resources");

		this.installData = installData;
		this.resources = resources;
		this.uninstallDataWriter = uninstallDataWriter;
		this.uninstallData = uninstallData;
		this.resourceHelper = new ResourcesHelper(installData, resources);
		this.resourceHelper.mergeCustomMessages();

		logger.log(Level.FINE, logPrefix + "Custom resources initialized");
	}

	@Override
	public void runAutomated(InstallData installData, IXMLElement panelRoot) {
		writeUninstallData();
	}

	private boolean writeUninstallData() {

		boolean result = true;

		boolean uninstallRequired = this.uninstallDataWriter.isUninstallRequired();
		logger.log(Level.FINE, logPrefix + "uninstallRequired:" + uninstallRequired);

		// We force the Uninstaller to be generated
		if (!uninstallRequired) {
			initUninstallPath(this.resources, this.installData);
			result = uninstallDataWriter.write();
			logger.log(Level.FINE,
					logPrefix + "force writeUninstallData. uninstallDataWriter.write() returns " + result);

			if (!result) {
				logger.warning(this.resourceHelper.getCustomString("installer.uninstall.writefailed"));
			}
		}
		return result;
	}

	public static void initUninstallPath(Resources resources, InstallData installData) {
		Info info = installData.getInfo();
		if (info.getUninstallerPath() == null) {
			String uninstallPath = info.getInstallationSubPath();
			if (uninstallPath == null)
				uninstallPath = installData.getInstallPath();
			if (uninstallPath == null && OsVersion.IS_WINDOWS)
				uninstallPath = installData.getVariable("TargetPanel.dir.windows");
			if (uninstallPath == null && OsVersion.IS_UNIX)
				uninstallPath = installData.getVariable("TargetPanel.dir.unix");

			info.setUninstallerPath(uninstallPath + File.separator + "Uninstaller");
		}
		if (info.getUninstallerName() == null) {
			info.setUninstallerName("uninstaller.jar");
		}

		// We need to clean up the uninstaller.jar to be able to regenerate it
		Path path = Paths.get(info.getUninstallerPath() + File.separator + info.getUninstallerName());
		try {
			Files.deleteIfExists(path);
			logger.log(Level.FINE, logPrefix + "Old uninstaller file " + path.toAbsolutePath() + " deleted");
		} catch (IOException e) {
			e.printStackTrace();
		}

		// createUninstallerIcon(resources, installData, "UninstallerIcon", "UninstallerIcon", ".ico");

	}
/*
	private static File createUninstallerIcon(Resources resources, InstallData installData, String resource,
			String targetfileName, String ext) {

		SpecHelper helper = new SpecHelper(resources);

		File tempFile = null;
		try {
			InputStream inputStream = helper.getResource(resource);

			if (inputStream == null) {
				logger.log(Level.FINE, logPrefix + "Cannot createTempFile(" + resource + ", " + targetfileName + ") ");
				return null;
			}

			logger.log(Level.FINE, logPrefix + "createTempFile(" + resource + ", " + targetfileName + ") ");

			tempFile = File.createTempFile(targetfileName, ext);
			tempFile.deleteOnExit();
			FileOutputStream fos = new FileOutputStream(tempFile);
			int a;
			while ((a = inputStream.read()) != -1)
				fos.write(a);
			fos.close();
			Path source =Paths.get(tempFile.getAbsolutePath()); 
			Path dest = Paths.get(installData.getInstallPath() + File.separator + "Uninstaller" + targetfileName + ext);
			logger.log(Level.FINE, logPrefix + "copy " + source + " to " + dest + "");
			Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
			
		} catch (com.izforge.izpack.api.exception.ResourceNotFoundException resNotFound) {
			logger.log(Level.WARNING, logPrefix + "Resource not found: " + resource + " " + resNotFound.getMessage());
			return null;
		} catch (com.izforge.izpack.api.exception.ResourceException resEx) {
			logger.log(Level.WARNING, logPrefix + "Resource error: " + resource + " " + resEx.getMessage());
			return null;
		} catch (IOException ex) {
			logger.log(Level.WARNING, logPrefix + "" + ex.getMessage());
			throw new InstallerException(
					logPrefix + "I/O error during writing resource " + resource + " to a temporary buildfile", ex);
		} catch (Exception ex) {
			logger.log(Level.WARNING, logPrefix + "Resource gerror: " + resource + " " + ex.getMessage());
			return null;
		} finally {
		}

		return tempFile;
	}
*/
}
