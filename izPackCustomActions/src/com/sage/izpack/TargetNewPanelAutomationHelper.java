package com.sage.izpack;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.panels.target.TargetPanelAutomation;

public class TargetNewPanelAutomationHelper extends TargetPanelAutomation {

	public TargetNewPanelAutomationHelper() {
		super();

	}

	@Override
	public void runAutomated(InstallData installData, IXMLElement panelRoot) {
		// We set the installation path
		IXMLElement ipath = panelRoot.getFirstChildNamed("installpath");
		String path = ipath.getContent();
		handleInstallPath(installData, path);
	}

	@Override
	public void processOptions(InstallData installData, Overrides overrides) {
		String path = overrides.fetch(InstallData.INSTALL_PATH);
		if (path != null) {
			handleInstallPath(installData, path);
		}
	}

	private void handleInstallPath(InstallData installData, String path)  {
		// Allow for variable substitution of the installpath value
		path = installData.getVariables().replace(path);

		if (InstallationInformationHelper.isIncompatibleInstallation(installData.getInstallPath(),
				installData.getInfo().isReadInstallationInformation())) {

			throw new InstallerException(installData.getMessages().get("TargetPanel.incompatibleInstallation"));
		}
		installData.setInstallPath(path);
	}
}
