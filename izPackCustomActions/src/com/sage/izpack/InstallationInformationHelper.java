package com.sage.izpack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.InstallerException;

public final class InstallationInformationHelper {

	private static final Logger logger = Logger.getLogger(InstallationInformationHelper.class.getName());

	public static void readInformation(com.izforge.izpack.api.data.InstallData installData) {

		logger.log(Level.FINE, "InstallationInformationHelper Reading file " + InstallData.INSTALLATION_INFORMATION);

		logger.log(Level.FINE, "InstallationInformationHelper Before " + InstallData.INSTALLATION_INFORMATION
				+ " component.node.name : " + installData.getVariable("component.node.name"));

		// Read .installationinformation
		// If old version 4.3.8, ... ?

		/*
		 * PacksModel model = new PacksModel(installData); Map<String, Pack>
		 * installedPacks = model.getInstalledPacks(); logger.info("Installed Packs : "
		 * + installedPacks);
		 * 
		 * for (Entry<String, Pack> entry : installedPacks.entrySet()) { Pack pack =
		 * entry.getValue(); logger.info("installedPacks: " + entry.getKey() +
		 * " - required: " + pack.isRequired() + "  Preselected: " +
		 * pack.isPreselected()); }
		 */
		Boolean informationloaded = false;
		try {
			logger.log(Level.FINE, "InstallationInformationHelper Loading installation information");
			loadInstallationInformation(installData);
			informationloaded = true;
			logger.log(Level.FINE, "InstallationInformationHelper Installation information loaded");
		} catch (Exception e) {
			logger.log(Level.FINE,
					"InstallationInformationHelper Installation information loading failed: " + e.getMessage());
			informationloaded = false;
		}

		if (!informationloaded) {
			logger.log(Level.FINE, "InstallationInformationHelper Loading legacy installation information");
			try {
				loadLegacyInstallationInformation(installData);
				logger.log(Level.FINE, "InstallationInformationHelper Legacy installation information loaded");

			} catch (Exception e) {
				logger.log(Level.FINE, "InstallationInformationHelper Installation legacy information loading failed: "
						+ e.getMessage());
				informationloaded = false;
			}
		}

		logger.log(Level.FINE, "After " + InstallData.INSTALLATION_INFORMATION + " component.node.name : "
				+ installData.getVariable("component.node.name"));

		logger.log(Level.FINE, "File " + InstallData.INSTALLATION_INFORMATION + " read.");

	}

	private static Map<String, Pack> loadInstallationInformation(com.izforge.izpack.api.data.InstallData installData)
			throws Exception {
		Map<String, Pack> readPacks = new HashMap<String, Pack>();

		// installation shall be modified
		// load installation information
		ObjectInputStream oin = null;
		File installInfo = new File(installData.getInstallPath(), InstallData.INSTALLATION_INFORMATION);
		if (installInfo.exists()) {
			FileInputStream fin = new FileInputStream(installInfo);
			oin = new ObjectInputStream(fin);
			// noinspection unchecked

			List<Pack> packsinstalled = (List<Pack>) oin.readObject();
			try {
				for (Pack installedpack : packsinstalled) {
					if (!readPacks.containsKey(installedpack.getName())) {
						readPacks.put(installedpack.getName(), installedpack);
						logger.log(Level.FINE,
								"InstallationInformationHelper Add pack " + installedpack.getName() + "");
					}
				}
				installData.setSelectedPacks(packsinstalled);
				// removeAlreadyInstalledPacks(installData.getSelectedPacks(), readPacks);
				// installData.setSelectedPacks(new ArrayList<Pack>());
				logger.log(Level.FINE,
						"InstallationInformationHelper Found " + packsinstalled.size() + " installed packs");
			} catch (Exception e) {
				logger.warning("InstallationInformationHelper Could not read Pack installation information: "
						+ e.getMessage());
			}

			try {
				Properties variables = (Properties) oin.readObject();
				for (Object key : variables.keySet()) {
					// if (key == "component.node.name") {
					installData.setVariable((String) key, (String) variables.get(key));
					logger.log(Level.FINE,
							"InstallationInformationHelper Set variable : " + key + ": " + variables.get(key));
				}
			} catch (Exception e) {
				logger.warning("InstallationInformationHelper Could not read Properties installation information: "
						+ e.getMessage());
				throw e;
			}

		}

		if (oin != null) {
			try {
				oin.close();
			} catch (IOException ignored) {
			}
		}
		return readPacks;
	}

	/*
	 * 
	 */
	private static void loadLegacyInstallationInformation(com.izforge.izpack.api.data.InstallData installData) {

		Map<String, com.sage.izpack.Pack> readPacks = new HashMap<String, com.sage.izpack.Pack>();

		ObjectInputStream oin = null;
		File installInfo = new File(installData.getInstallPath(), InstallData.INSTALLATION_INFORMATION);
		if (installInfo.exists()) {
			FileInputStream fin;
			try {
				fin = new FileInputStream(installInfo);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				return;
			}
			try {
				oin = new ObjectInputStream(fin);
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}

			List<com.sage.izpack.Pack> packsinstalled;
			try {
				packsinstalled = (List<com.sage.izpack.Pack>) oin.readObject();

				ArrayList<Pack> s = new ArrayList<Pack>();

				for (com.sage.izpack.Pack installedpack : packsinstalled) {
					readPacks.put(installedpack.name, installedpack);
					s.add(new Pack(installedpack.name, installedpack.id, installedpack.description, null,
							installedpack.dependencies, installedpack.required, installedpack.preselected,
							installedpack.loose, installedpack.excludeGroup, installedpack.uninstall, 0));

					logger.log(Level.FINE,
							"InstallationInformationHelper.loadLegacyInstallationInformation - installedpack: "
									+ installedpack.name);
				}
				// removeAlreadyInstalledPacks(installData.getSelectedPacks(), readPacks);

				installData.setSelectedPacks(s);
				// installData.setSelectedPacks(new ArrayList<Pack>());
				logger.log(Level.FINE, "InstallationInformationHelper.loadLegacyInstallationInformation - Found Legacy "
						+ packsinstalled.size() + " installed packs");
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (Exception e) {
				logger.warning(
						"InstallationInformationHelper.loadLegacyInstallationInformation  Could not read Legacy 'Pack' installation information: "
								+ e.getMessage());
			}

			try {
				Properties variables = (Properties) oin.readObject();
				for (Object key : variables.keySet()) {
					installData.setVariable((String) key, (String) variables.get(key));
					logger.log(Level.FINE,
							"InstallationInformationHelper.loadLegacyInstallationInformation  Set Legacy variable : "
									+ key + ": " + variables.get(key));
				}

				logger.log(Level.FINE,
						"InstallationInformationHelper.loadLegacyInstallationInformation  writeInstallationInformation to fix legacy issue");
				// writeInstallationInformation(installData, installData.getSelectedPacks(),
				// installData.getVariables());
				writeInstallationInformation(installData, installData.getSelectedPacks(), installData.getVariables());

			} catch (Exception e) {
				logger.warning(
						"InstallationInformationHelper.loadLegacyInstallationInformation  Could not read Legacy 'Properties' installation information: "
								+ e.getMessage());
			}
		}
	}

	private static void writeInstallationInformation(com.izforge.izpack.api.data.InstallData installData,
			List<Pack> selectedPacks, Variables variables) throws IOException {

		if (!installData.getInfo().isWriteInstallationInformation()) {
			logger.log(Level.FINE, "InstallationInformationHelper  Skip writing installation information");
			return;
		}
		logger.log(Level.FINE, "InstallationInformationHelper  Writing installation information to fix legacy issue");
		String installDir = installData.getInstallPath();

		// List<Pack> installedPacks = new ArrayList<Pack>(selectedPacks);

		File installationInfo = new File(installDir + File.separator + InstallData.INSTALLATION_INFORMATION);
		if (!installationInfo.exists()) {
			logger.log(Level.FINE,
					"InstallationInformationHelper  Creating info file " + installationInfo.getAbsolutePath());
			File dir = new File(installData.getInstallPath());
			if (!dir.exists()) {
				// if no packs have been installed, then the installation directory won't exist
				if (!dir.mkdirs()) {
					throw new InstallerException("Failed to create directory: " + dir);
				}
			}
			if (!installationInfo.createNewFile()) {
				throw new InstallerException("Failed to create file: " + installationInfo);
			}
		} else {
			logger.log(Level.FINE, "InstallationInformationHelper  Previous installation information found: "
					+ installationInfo.getAbsolutePath());
			// read in old information and update
			// FileInputStream fin = new FileInputStream(installationInfo);
			// ObjectInputStream oin = new ObjectInputStream(fin);

			/*
			 * List<Pack> packs; try { //noinspection unchecked packs = (List<Pack>)
			 * oin.readObject(); } catch (Exception exception) { throw new
			 * InstallerException("Failed to read previous installation information",
			 * exception); } finally { IOUtils.closeQuietly(oin); IOUtils.closeQuietly(fin);
			 * }
			 */
			// installedPacks.addAll(packs);
		}

		FileOutputStream fout = new FileOutputStream(installationInfo);
		ObjectOutputStream oout = new ObjectOutputStream(fout);
		oout.writeObject(selectedPacks);
		// oout.writeObject(variables.getProperties());
		oout.writeObject(variables.getProperties());
		fout.close();

		logger.log(Level.FINE, "Installation information saved: " + installationInfo.getAbsolutePath());
		// IOUtils.closeQuietly(oout);
		// IOUtils.closeQuietly(fout);

		// uninstallData.addFile(installationInfo.getAbsolutePath(), true);
	}

}
