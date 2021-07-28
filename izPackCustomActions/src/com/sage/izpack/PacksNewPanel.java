package com.sage.izpack;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JScrollPane;
import javax.swing.JTable;

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

	/**
	 * Creates the table for the packs. All parameters are required. The table will
	 * be returned.
	 *
	 * @param width       of the table
	 * @param scroller    the scroller to be used
	 * @param layout      layout to be used
	 * @param constraints constraints to be used
	 * @return the created table
	 */
	@Override
	protected JTable createPacksTable(int width, JScrollPane scroller, GridBagLayout layout,
			GridBagConstraints constraints) {
		logger.info("PacksNewPanel.createPacksTable");

		return super.createPacksTable(width, scroller, layout, constraints);
	}

	/**
	 * Called when the panel becomes active. If a derived class implements this
	 * method also, it is recommended to call this method with the super operator
	 * first.
	 */
	@Override
	public void panelActivate() {
		logger.info("PacksNewPanel.panelActivate : installData.getAvailablePacks()");

		for (Pack p : this.installData.getAvailablePacks()) {

			logger.info("PacksNewPanel.panelActivate - Pack " + p.getName() + " Required: " + p.isRequired()
					+ " Preselected: " + p.isPreselected());

			if (p.isRequired()) {
				p.setPreselected(true);
			}
		}
		super.panelActivate();
	}

	/**
	 * Indicates whether the panel has been validated or not.
	 *
	 * @return true if the needed space is less than the free space, else false
	 */
	@Override
	public boolean isValidated() {
		logger.info("PacksNewPanel.isValidated : ");

		boolean isValidated = super.isValidated();
		logger.info("PacksNewPanel.isValidated : " + isValidated);

		return isValidated;
	}

	@Override
	public String getSummaryBody() {
		StringBuilder retval = new StringBuilder(256);
		boolean first = true;

		logger.info("PacksNewPanel.getSummaryBody : installData.getSelectedPacks()");

		for (com.izforge.izpack.api.data.Pack pack : installData.getSelectedPacks()) {
			if (!first) {
				retval.append("<br>");
			}
			first = false;
			retval.append(getI18NPackName(pack));
		}

		logger.info("PacksNewPanel.getSummaryBody : packsModel.isModifyInstallation()");

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

		logger.info("PacksNewPanel.getSummaryBody : " + retval.toString());

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
