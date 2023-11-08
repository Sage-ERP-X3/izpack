package com.sage.izpack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.panels.finish.FinishPanelAutomation;
import com.izforge.izpack.util.OsVersion;

public class FinishNewPanelAutomationHelper extends FinishPanelAutomation {

	private static Logger logger = Logger.getLogger(FinishNewPanelAutomationHelper.class.getName());

	private ResourcesHelper _resourceHelper;

	public FinishNewPanelAutomationHelper(InstallData installData, Resources resources) throws NativeLibException {
		super();

		logger.log(Level.FINE, "FinishNewPanelAutomationHelper instance. Init custom resources");

		_resourceHelper = new ResourcesHelper(installData, resources);
		_resourceHelper.mergeCustomMessages();

		logger.log(Level.FINE, "FinishNewPanelAutomationHelper instance. Custom resources initialized");
	}

	public static void initUninstallPath(InstallData installData) {
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
		
		Path path = Paths.get(info.getUninstallerPath() + File.separator + info.getUninstallerName());
		try {
			Files.deleteIfExists(path);
			logger.log(Level.FINE, "Old uninstaller file " + path.toAbsolutePath() + " deleted");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
