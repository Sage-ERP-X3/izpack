package com.sage.izpack;

import java.util.Map;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.util.PackHelper;
import com.izforge.izpack.panels.packs.PacksPanel;

public class PacksNewPanel extends PacksPanel {

	private static final long serialVersionUID = 2809544763635023846L;

	private static final Logger logger = Logger.getLogger(PacksNewPanel.class.getName());

	/**
	 * The packs messages.
	 */
	private Messages messages = null;

	public PacksNewPanel(Panel arg0, InstallerFrame arg1, GUIInstallData installData, Resources resources,
			ObjectFactory factory, RulesEngine rules) {
		super(arg0, arg1, installData, resources, factory, rules);
	}

	@Override
	public String getSummaryBody() {
		StringBuilder retval = new StringBuilder(256);
		boolean first = true;

		logger.info("getSummaryBody : installData.getSelectedPacks()");

		for (com.izforge.izpack.api.data.Pack pack : installData.getSelectedPacks()) {
			if (!first) {
				retval.append("<br>");
			}
			first = false;
			retval.append(getI18NPackName(pack));
		}

		logger.info("getSummaryBody : packsModel.isModifyInstallation()");

		if (packsModel.isModifyInstallation()) {
			Map<String, com.izforge.izpack.api.data.Pack> installedpacks = packsModel.getInstalledPacks();
			retval.append("<br><b>");
			retval.append(messages.get("PacksPanel.installedpacks.summarycaption"));
			retval.append("</b>");
			retval.append("<br>");
			if (installedpacks != null)
				for (String key : installedpacks.keySet()) {
					Pack pack = installedpacks.get(key);
					retval.append(getI18NPackName(pack));
					retval.append("<br>");
				}
		}

		logger.info("getSummaryBody : " + retval.toString());

		return (retval.toString());
	}

	/**
	 * This method tries to resolve the localized name of the given pack. If this is
	 * not possible, the name given in the installation description file in ELEMENT
	 * <pack> will be used.
	 *
	 * @param pack for which the name should be resolved
	 * @return localized name of the pack
	 */
	private String getI18NPackName(com.izforge.izpack.api.data.Pack pack) {
		if (messages == null) {
			try {
				messages = installData.getMessages().newMessages(Resources.PACK_TRANSLATIONS_RESOURCE_NAME);
			} catch (ResourceNotFoundException exception) {
				// no packs messages resource, so fall back to the default
				logger.info(exception.getMessage());
				messages = installData.getMessages();
			}
		}

		return PackHelper.getPackName(pack, messages);
	}

}
