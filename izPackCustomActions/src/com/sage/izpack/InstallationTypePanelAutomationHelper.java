package com.sage.izpack;

import com.coi.tools.os.win.MSWinConstants;
import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.installer.automation.PanelAutomation;

public class InstallationTypePanelAutomationHelper extends PanelAutomationHelper
		implements PanelAutomation, MSWinConstants {

	@Override
	public void createInstallationRecord(InstallData installData, IXMLElement panelRoot) {
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

	}

	@Override
	public void runAutomated(InstallData installData, IXMLElement panelRoot) throws InstallerException {
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

	}

	@Override
	public int askWarningQuestion(String title, String question, int choices, int default_choice) {
		return 0;
	}

	@Override
	public void processOptions(InstallData installData, Overrides overrides) {
		
	}

}
