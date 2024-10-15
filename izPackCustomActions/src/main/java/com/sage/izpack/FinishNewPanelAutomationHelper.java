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

		if (installData == null) {
			logger.log(Level.SEVERE, logPrefix + " Error: initUninstallPath(resources, installData = NULL)");

			return;
		}
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
	}

}
