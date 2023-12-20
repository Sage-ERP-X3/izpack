package com.sage.izpack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

// import com.izforge.izpack.api.data.Pack; 
// com.izforge.izpack.Pack; 
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;

public class PreValidatePacksPanelAction implements com.izforge.izpack.data.PanelAction {

	private static final Logger logger = Logger.getLogger(PreValidatePacksPanelAction.class.getName());
	private static final String prefixLabel = "PreValidatePacksPanelAction - ";
	private Map<String, com.izforge.izpack.Pack> installedpacks = null;

	@Override
	public void executeAction(InstallData installData, AbstractUIHandler arg1) {
		// well if in modify mode
		// then we must select already installed packs


		
		// Avoid reset of our correct values and variables already read
		// boolean hasAlreadyReadInfo =
		// InstallationInformationHelper.hasAlreadyReadInformation(installData);
		// if (installData.getInfo().isReadInstallationInformation()) {
		//	logger.log(Level.FINE, prefixLabel + "setReadInstallationInformation to false. Variables already read");
		//	installData.getInfo().setReadInstallationInformation(false);
		//}

		boolean modifyinstallation = Boolean.valueOf(installData.getVariable(InstallData.MODIFY_INSTALLATION));
		this.installedpacks = new HashMap<String, com.izforge.izpack.Pack>();
		HashMap<String, com.izforge.izpack.api.data.Pack> installedpacksBis = new HashMap<String, com.izforge.izpack.api.data.Pack>();
		if (modifyinstallation) {
			// installation shall be modified
			// load installation information
			logger.log(Level.FINE, prefixLabel + "Update mode, loading installed packs ...");

			try {
				File file = new File(
						installData.getInstallPath() + File.separator + AutomatedInstallData.INSTALLATION_INFORMATION);
				FileInputStream fin = new FileInputStream(file);
				ObjectInputStream oin = new ObjectInputStream(fin);
				List<?> packsinstalled = (List<?>) oin.readObject();
				for (Object aPacksinstalled : packsinstalled) {
					if (aPacksinstalled instanceof com.izforge.izpack.Pack) {
						// com.izforge.izpack.api.data.Pack cannot be cast to com.izforge.izpack.Pack
						com.izforge.izpack.Pack installedpack = (com.izforge.izpack.Pack) aPacksinstalled;
						installedpack.setHidden(false);
						installedpacks.put(installedpack.name, installedpack);
						logger.log(Level.FINE, prefixLabel + "found pack " + installedpack.name);
						// }
					}

					if (aPacksinstalled instanceof com.izforge.izpack.api.data.Pack) {
						// com.izforge.izpack.api.data.Pack cannot be cast to com.izforge.izpack.Pack
						com.izforge.izpack.api.data.Pack installedpack = (com.izforge.izpack.api.data.Pack) aPacksinstalled;
						installedpack.setPreselected(true);
						if (installedpacksBis.get(installedpack.getName()) == null)
							installedpacksBis.put(installedpack.getName(), installedpack);
						// installedpacks.put(installedpack.getName(), installedpack);
						logger.log(Level.FINE, prefixLabel + "found " + installedpack.getName());
					}
				}

				// this.removeAlreadyInstalledPacks(installData.getSelectedPacks());
				// TODO: FRDEPO
				// adata.installedPacks = packsinstalled;

				List<com.izforge.izpack.api.data.Pack> packages = new ArrayList<com.izforge.izpack.api.data.Pack>();
				for (com.izforge.izpack.api.data.Pack aPack : installData.getAvailablePacks()) {
					if (installedpacks.containsKey(aPack.getName()) || installedpacksBis.containsKey(aPack.getName())) {
						aPack.setPreselected(true);
						// aPack.setRequired()= true;
						if (!packages.contains(aPack.getName())) {
							logger.log(Level.FINE, prefixLabel + "Add package '" + aPack.getName() + "' as selected");
							packages.add(aPack);
						}
					}
				}
				installData.setSelectedPacks(packages);

				logger.log(Level.FINE, prefixLabel + "Found " + packsinstalled.size()
						+ " packs in informationInstallation. " + packages.size() + " selected packages.");
				logger.log(Level.FINE, prefixLabel + "Loading properties ...");

				Properties variables = (Properties) oin.readObject();

				Iterator<?> iter = variables.keySet().iterator();
				while (iter.hasNext()) {
					String key = (String) iter.next();
					// if (Character.isLowerCase(key.charAt(0)) || "UNINSTALL_NAME".equals(key)) {
					if (key.equals("UNINSTALL_NAME")) {
						installData.setVariable(key, (String) variables.get(key));
						logger.log(Level.FINE, prefixLabel + (String) key + "=" + (String) variables.get(key));
					}

					// TODO : verrue !
					// if (key.equals("syracuse.winservice.username"))
					// {
					// installData.setVariable( "syracuse.winservice.username.oldvalue", (String)
					// variables.get(key));
					// }
				}

				fin.close();

			} catch (FileNotFoundException e) {
				logger.log(Level.WARNING, e.getMessage());
				e.printStackTrace();
			} catch (java.io.IOException e) {
				logger.log(Level.WARNING, e.getMessage());
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				logger.log(Level.WARNING, e.getMessage());
				e.printStackTrace();
			}
		}

	}

	@Override
	public void initialize(PanelActionConfiguration configuration) {
		// nothing to do really

	}

	private void removeAlreadyInstalledPacks(List<com.izforge.izpack.api.data.Pack> selectedpacks) {
		List<com.izforge.izpack.api.data.Pack> removepacks = new ArrayList<com.izforge.izpack.api.data.Pack>();

		for (com.izforge.izpack.api.data.Pack selectedpack : selectedpacks) {
			String key = selectedpack.getName();
			/*
			 * if ((selectedpack.getImageId() != null) &&
			 * (selectedpack.getImageId().length() > 0)) key = selectedpack.getImageId();
			 * else key = selectedpack.getName();
			 */
			if (installedpacks.containsKey(key)) {
				// pack is already installed, remove it
				removepacks.add(selectedpack);
			}
		}
		for (com.izforge.izpack.api.data.Pack removepack : removepacks) {
			selectedpacks.remove(removepack);
		}
	}

}
