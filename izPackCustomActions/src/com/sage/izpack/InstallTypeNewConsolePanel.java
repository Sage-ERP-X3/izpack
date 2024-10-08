package com.sage.izpack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.config.Options;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.handler.PromptUIHandler;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.panels.checkedhello.RegistryHelper;

/**
 *
 * @author Franck DEPOORTERE
 */
public class InstallTypeNewConsolePanel extends com.izforge.izpack.installer.console.AbstractTextConsolePanel {

	private static Logger logger = Logger.getLogger(InstallTypeNewConsolePanel.class.getName());
	private final Resources resources;
	private final PromptUIHandler handler;
	private RegistryHelper registryHelper;
	private RegistryHandler registryHandler;
	private InstallData installData;

	public InstallTypeNewConsolePanel(RegistryDefaultHandler handler, InstallData installData, Resources resources,
			Prompt prompt, PanelView<ConsolePanel> panel) {
		super(panel);
		this.registryHelper = new RegistryHelper(handler, installData);
		this.registryHandler = handler != null ? handler.getInstance() : null;
		this.resources = resources;
		this.handler = new PromptUIHandler(prompt);
		this.installData = installData;
		logger.log(Level.FINE, "InstallTypeNewConsolePanel instance.");
	}

	@Override
	public boolean run(InstallData installData, Properties p) {
		String strType = p.getProperty(InstallData.MODIFY_INSTALLATION.toUpperCase()).trim();
		if (strType == null || "".equals(strType)) {
			strType = p.getProperty(InstallData.MODIFY_INSTALLATION.toLowerCase()).trim();
		}
		if (strType == null || "".equals(strType)) {
			// assume a normal install
			ModifyInstallationUtil.set(installData, Boolean.FALSE);
		} else {
			if (Boolean.parseBoolean(strType)) {
				// is a modify type install
				ModifyInstallationUtil.set(installData, Boolean.TRUE);
				String strInstallpath = p.getProperty("installpath").trim();
				installData.setInstallPath(strInstallpath);

			} else {
				ModifyInstallationUtil.set(installData, Boolean.FALSE);
			}
		}
		return true;
	}

	/**
	 * Prompts to end the Install panel.
	 * <p/>
	 * This displays a prompt "Press 1 for a new install, 2 to update, 3 to exit"
	 *
	 *
	 * @param installData the installation date
	 * @param console     the console to use
	 * @return {@code true} to accept, {@code false} to reject. If the panel is
	 *         displayed again, the result of {@link #run(InstallData, Console)} is
	 *         returned
	 */
	@Override
	protected boolean promptEndPanel(InstallData installData, com.izforge.izpack.util.Console console) {

		ResourcesHelper resourcesHelper = new ResourcesHelper(installData, this.resources);
		String str = resourcesHelper.getCustomString("InstallTypeNewPanel.info");
		System.out.println("");
		System.out.println(str);

		int i = 0;
		while (i < 1 || i > 3) {
			i = console.prompt(resourcesHelper.getCustomString("InstallTypeNewPanel.asktype"), 1, 2, 3);
			// i = this.handler.askQuestion("",
			// resourcesHelper.getCustomString("InstallTypeNewPanel.asktype"), 3);
		}

		switch (i) {
		case 1:
			ModifyInstallationUtil.set(installData, Boolean.FALSE);
			return true;
		case 2:
			ModifyInstallationUtil.set(installData, Boolean.TRUE);
			return chooseComponent(installData, console);
		default:
			// want to exit
			return false;
		}
	}

	@Override
	public void createInstallationRecord(IXMLElement panelRoot) {

		new InstallTypeNewPanelAutomation().createInstallationRecord(this.installData, panelRoot);
		logger.log(Level.FINE, "InstallTypeNewConsolePanel createInstallationRecord.");

	}

	private boolean chooseComponent(InstallData installData, com.izforge.izpack.util.Console console) {
		try {
			ResourcesHelper resourcesHelper = new ResourcesHelper(installData, this.resources);
			String strQuestion = resourcesHelper.getCustomString("InstallTypeNewPanel.askUpdatePath");
			RegistryHandlerX3 helper = new RegistryHandlerX3(this.registryHandler, installData);
			HashMap<String, String[]> installedProducts = helper.loadComponentsList();
			List<String> keysArray = new ArrayList<>();

			System.out.println();

			int i = 0;
			for (Map.Entry<String, String[]> pair : installedProducts.entrySet()) {
				keysArray.add(pair.getKey());
				// String str = Arrays.toString(pair.getValue());
				// System.out.format("key: %s, value: %s%n", pair.getKey(), str);
				System.out.println(i + " - " + pair.getKey());
				i++;
			}
			System.out.println();

			while (true) {

				int j = console.prompt(strQuestion, 0, installedProducts.size() - 1, installedProducts.size());

				if (j > -1 && j < installedProducts.size()) {
					String key = keysArray.get(j);
					String[] product = installedProducts.get(key);
					String installPath = null;
					if (product.length > 1) {
						installPath = product[1];
						installData.setInstallPath(installPath);
						logger.log(Level.FINE, "User selected id " + j + " key: " + key + " value: "
							+ Arrays.toString(product) + " installPath:" + installPath);
					} else {

						logger.log(Level.SEVERE, "Cannot get InstallPath -  User selected id " + j + " key: " + key
							+ " value: " + Arrays.toString(product) + " installPath:" + installPath);
						return false;
					}
					return true;
				} else {
					System.out.println(resourcesHelper.getCustomString("UserInputPanel.search.wrongselection.caption"));
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return false;
	}

	@Override
	public boolean generateOptions(InstallData installData, Options arg1) {

		logger.log(Level.FINE, "InstallTypeNewConsolePanel generateOptions.");
		return false;
	}

	@Override
	public boolean handlePanelValidationResult(boolean arg0) {

		logger.log(Level.FINE, "InstallTypeNewConsolePanel handlePanelValidationResult.");

		return true;
	}

	@Override
	protected String getText() {
		logger.log(Level.FINE, "InstallTypeNewConsolePanel getText.");
		return null;
	}

}
