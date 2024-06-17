package com.sage.izpack;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.installer.automation.PanelAutomation;

public class InstallTypeNewPanelAutomation implements PanelAutomation {

	private static final String INSTALLPATH = "installpath";

	@Override
	public void createInstallationRecord(InstallData installData, IXMLElement panelRoot) {
		// part of MODIFY_INSTALLATION
		IXMLElement ipath = new XMLElementImpl(InstallData.MODIFY_INSTALLATION, panelRoot);
		// check this writes even if value is the default,
		// because without the constructor, default does not get set.
		if (installData.getVariable(InstallData.MODIFY_INSTALLATION) != null) {
			ipath.setContent(installData.getVariable(InstallData.MODIFY_INSTALLATION));
		} else {
			ipath.setContent(Boolean.FALSE.toString());
		}

		IXMLElement prev = panelRoot.getFirstChildNamed(InstallData.MODIFY_INSTALLATION);
		if (prev != null) {
			panelRoot.removeChild(prev);
		}
		panelRoot.addChild(ipath);

		// part of target path
		IXMLElement ipath2 = new XMLElementImpl(INSTALLPATH, panelRoot);
		ipath2.setContent(installData.getInstallPath());

		IXMLElement prev2 = panelRoot.getFirstChildNamed(INSTALLPATH);
		if (prev2 != null) {
			panelRoot.removeChild(prev2);
		}
		panelRoot.addChild(ipath2);

	}

	@Override
	public void runAutomated(InstallData installData, IXMLElement panelRoot) throws InstallerException {

		// part of MODIFY_INSTALLATION
		IXMLElement ipath = panelRoot.getFirstChildNamed(InstallData.MODIFY_INSTALLATION);

		String modify = null;

		try {
			modify = ipath.getContent().trim();
		} catch (Exception ex) {
			// assume a normal install
			installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
		}

		if (modify == null || "".equals(modify)) {
			// assume a normal install
			installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
		} else {
			if (Boolean.parseBoolean(modify)) {
				installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");
			} else {
				installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
			}
		}
		// part of target path
		IXMLElement ipath2 = panelRoot.getFirstChildNamed(INSTALLPATH);

		String installpath = null;

		try {
			installpath = ipath2.getContent().trim();
			installData.setInstallPath(installpath);

			System.out.println();
			System.out.println(ResourcesHelper.getCustomPropString("TargetPanel.summaryCaption"));
			System.out.println(installpath);
			System.out.println();

		} catch (Exception ex) {
			// assume a normal install
			throw new InstallerException(ex.getLocalizedMessage());
		}

	}

	@Override
	public void processOptions(InstallData installData, Overrides overrides) {
		String modifyInstallation = overrides.fetch(InstallData.MODIFY_INSTALLATION);
		if (modifyInstallation == null) {
			modifyInstallation = overrides.fetch(InstallData.MODIFY_INSTALLATION.toUpperCase());
		}
		if (modifyInstallation != null) {
			installData.setVariable(InstallData.MODIFY_INSTALLATION,
					String.valueOf(Boolean.parseBoolean(modifyInstallation)));
		}
		String installpath = overrides.fetch(INSTALLPATH);
		if (installpath != null) {
			installData.setVariable(INSTALLPATH, installpath.trim());
		}
	}

}
