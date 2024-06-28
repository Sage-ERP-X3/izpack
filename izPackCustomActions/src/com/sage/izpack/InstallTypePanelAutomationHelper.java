package com.sage.izpack;

import com.coi.tools.os.win.MSWinConstants;
import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.installer.automation.PanelAutomation;

public class InstallTypePanelAutomationHelper extends PanelAutomationHelper implements PanelAutomation, MSWinConstants {

	@Override
	public void createInstallationRecord(InstallData installData, IXMLElement panelRoot) {
		// part of MODIFY_INSTALLATION
		InstallationTypePanelAutomationHelper automationHelper = new InstallationTypePanelAutomationHelper();
		automationHelper.createInstallationRecord(installData, panelRoot);

		// part of target path

		IXMLElement ipath = new XMLElementImpl("installpath", panelRoot);
		ipath.setContent(installData.getInstallPath());

		IXMLElement prev = panelRoot.getFirstChildNamed("installpath");
		if (prev != null) {
			panelRoot.removeChild(prev);
		}
		panelRoot.addChild(ipath);

	}

	@Override
	public void runAutomated(InstallData installData, IXMLElement panelRoot) throws InstallerException {

		// part of MODIFY_INSTALLATION
		InstallationTypePanelAutomationHelper automationHelper = new InstallationTypePanelAutomationHelper();
		automationHelper.runAutomated(installData, panelRoot);

		// part of target path
		IXMLElement ipath = panelRoot.getFirstChildNamed("installpath");

		String installpath = null;

		try {
			installpath = ipath.getContent().trim();
			installData.setInstallPath(installpath);

		} catch (Exception ex) {
			// assume a normal install
			throw new InstallerException(ex.getLocalizedMessage());
		}

	}

	@Override
	public int askWarningQuestion(String title, String question, int choices, int default_choice) {
		return 0;
	}

	@Override
	public void processOptions(InstallData installData, Overrides overrides) {
		
	}

}
