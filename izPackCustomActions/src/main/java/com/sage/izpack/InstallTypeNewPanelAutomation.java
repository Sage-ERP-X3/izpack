package com.sage.izpack;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.installer.automation.PanelAutomation;


public class InstallTypeNewPanelAutomation implements PanelAutomation {

	private static final String INSTALLPATH = "installpath";
	public static final String COMPONENT_NODE_NAME = "component.node.name";
	public static final String COMPONENT_NODE_TYPE = "component.node.type";
	public static final String NEED_SERVICE_CONFIGURATION_FIX = "need-service-configuration-fix";

	@Override
	public void createInstallationRecord(InstallData installData, IXMLElement panelRoot) {
		// part of MODIFY_INSTALLATION
		IXMLElement ipath = new XMLElementImpl(InstallData.MODIFY_INSTALLATION, panelRoot);
		// check this writes even if value is the default,
		// because without the constructor, default does not get set.
		Boolean isModify = ModifyInstallationUtil.get(installData);
		ipath.setContent(isModify.toString());

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

		/*
		 * component.node.(name|type) are not being set properly during unattended upgrade.
		 * this is a workaround, since they are working properly in gui and console and you
		 * can only generate auto-install.xml in those modes, we save those variables to be
		 * used in unattended mode - where they are not being read.
		 */
		boolean isSpecialBehavior = Boolean.parseBoolean(installData.getVariable(NEED_SERVICE_CONFIGURATION_FIX));
		// installer needs to declare special variable to enable this behavior
		if (!isModify || !isSpecialBehavior) {
		    // we only need this on upgrade, on fresh install there is a panel that has this data
			return;
		}
		PacksNewPanelAutomationHelper.readInstallationInformation(installData);
		String componentName = installData.getVariable(COMPONENT_NODE_NAME);
		if (componentName != null && !componentName.isBlank()) { // installer might not declare/use this var
			IXMLElement prevName = panelRoot.getFirstChildNamed(COMPONENT_NODE_NAME);
			if (prevName != null) {
				panelRoot.removeChild(prevName);
			}
			IXMLElement nodeName = new XMLElementImpl(COMPONENT_NODE_NAME, panelRoot);
			nodeName.setContent(componentName);
			panelRoot.addChild(nodeName);
		}
		String componentType = installData.getVariable(COMPONENT_NODE_TYPE);
		if (componentType != null && !componentType.isBlank()) { // installer might not declare/use this var
			IXMLElement prevType = panelRoot.getFirstChildNamed(COMPONENT_NODE_TYPE);
			if (prevType != null) {
				panelRoot.removeChild(prevType);
			}
			IXMLElement nodeType = new XMLElementImpl(COMPONENT_NODE_TYPE, panelRoot);
			nodeType.setContent(componentType);
			panelRoot.addChild(nodeType);
		}
	}

	@Override
	public void runAutomated(InstallData installData, IXMLElement panelRoot) throws InstallerException {
		boolean isModify = ModifyInstallationUtil.get(panelRoot);
		// part of MODIFY_INSTALLATION
		ModifyInstallationUtil.set(installData, isModify);
		// part of target path
		IXMLElement ipath2 = panelRoot.getFirstChildNamed(INSTALLPATH);

		String installpath;

		try {
			installpath = ipath2.getContent().trim();
			installData.setInstallPath(installpath);
		} catch (Exception ex) {
			// assume a normal install
			throw new InstallerException(ex.getLocalizedMessage());
		}
		System.out.println();
		System.out.println(installpath);
		System.out.println();

		boolean isSpecialBehavior = Boolean.parseBoolean(installData.getVariable(NEED_SERVICE_CONFIGURATION_FIX));
		if (isSpecialBehavior && isModify) {
			IXMLElement nodeName = panelRoot.getFirstChildNamed(COMPONENT_NODE_NAME);
			if (nodeName != null && nodeName.getContent() != null) {
				installData.setVariable(COMPONENT_NODE_NAME, nodeName.getContent().trim());
			}
			IXMLElement nodeType = panelRoot.getFirstChildNamed(COMPONENT_NODE_TYPE);
			if (nodeType != null && nodeType.getContent() != null) {
				installData.setVariable(COMPONENT_NODE_TYPE, nodeType.getContent().trim());
			}
		}
	}

	@Override
	public void processOptions(InstallData installData, Overrides overrides) {
		String modifyInstallation = overrides.fetch(InstallData.MODIFY_INSTALLATION);
		if (modifyInstallation == null) {
			modifyInstallation = overrides.fetch(InstallData.MODIFY_INSTALLATION.toUpperCase());
		}
		if (modifyInstallation != null) {
			ModifyInstallationUtil.set(installData, modifyInstallation);
		}
		String installpath = overrides.fetch(INSTALLPATH);
		if (installpath != null) {
			installData.setVariable(INSTALLPATH, installpath.trim());
		}

		boolean isSpecialBehavior = Boolean.parseBoolean(installData.getVariable(NEED_SERVICE_CONFIGURATION_FIX));
		if (isSpecialBehavior && Boolean.parseBoolean(modifyInstallation)) {
			String nodeName = overrides.fetch(COMPONENT_NODE_NAME);
			if (nodeName != null) {
				installData.setVariable(COMPONENT_NODE_NAME, nodeName.trim());
			}
			String nodeType = overrides.fetch(COMPONENT_NODE_TYPE);
			if (nodeType != null) {
				installData.setVariable(COMPONENT_NODE_TYPE, nodeType.trim());
			}
		}
	}

}
