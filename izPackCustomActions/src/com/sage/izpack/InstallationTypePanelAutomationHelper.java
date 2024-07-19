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
		ipath.setContent(ModifyInstallationUtil.get(installData).toString());

		IXMLElement prev = panelRoot.getFirstChildNamed(InstallData.MODIFY_INSTALLATION);
		if (prev != null) {
			panelRoot.removeChild(prev);
		}
		panelRoot.addChild(ipath);

	}

	@Override
	public void runAutomated(InstallData installData, IXMLElement panelRoot) throws InstallerException {
		ModifyInstallationUtil.set(installData, ModifyInstallationUtil.get(panelRoot));
	}

	@Override
	public int askWarningQuestion(String title, String question, int choices, int default_choice) {
		return 0;
	}

	@Override
	public void processOptions(InstallData installData, Overrides overrides) {
		
	}

}
