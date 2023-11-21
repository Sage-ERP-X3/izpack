package com.sage.izpack;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.resource.Resources;

public class ReadMeNewPanelAutomationHelper implements com.izforge.izpack.installer.automation.PanelAutomation {

	private static Logger logger = Logger.getLogger(ReadMeNewPanelAutomationHelper.class.getName());
	private static String LogPrefix = "ReadMeNewPanelAutomationHelper - ";

	private ResourcesHelper _resourceHelper;

	public ReadMeNewPanelAutomationHelper(InstallData installData, Resources resources) throws NativeLibException {
		super();

		logger.log(Level.FINE, LogPrefix + "instance. Init custom resources");

		_resourceHelper = new ResourcesHelper(installData, resources);
		_resourceHelper.mergeCustomMessages();

		logger.log(Level.FINE, LogPrefix + "instance. Custom resources initialized");
	}

	@Override
	public void createInstallationRecord(InstallData arg0, IXMLElement arg1) {
		// Nothing to do

	}

	@Override
	public void processOptions(InstallData arg0, Overrides arg1) {
		// Nothing to do

	}

	@Override
	public void runAutomated(InstallData arg0, IXMLElement arg1) throws InstallerException {
		// Nothing to do

	}

}
