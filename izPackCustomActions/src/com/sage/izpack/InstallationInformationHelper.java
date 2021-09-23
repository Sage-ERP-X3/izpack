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

/*
 * 
 * @author Franck DEPOORTERE
 */
public final class InstallationInformationHelper {

	private static final Logger logger = Logger.getLogger(InstallationInformationHelper.class.getName());

	public static boolean hasAlreadyReadInformation(com.izforge.izpack.api.data.InstallData installData) {

		String informationRead = installData.getVariable("information-read");
		boolean result = false;
		if (informationRead != null && informationRead.equalsIgnoreCase("true")) {
			result = true;
		}

		logger.log(Level.FINE,
				"InstallationInformationHelper hasAlreadyReadInformation : " + result + " value: " + informationRead);
		return result;
	}

	public static boolean readInformation(com.izforge.izpack.api.data.InstallData installData) {

		logger.log(Level.FINE, "InstallationInformationHelper Reading file " + InstallData.INSTALLATION_INFORMATION);

		logger.log(Level.FINE, "InstallationInformationHelper Before " + InstallData.INSTALLATION_INFORMATION
				+ " component.node.name : " + installData.getVariable("component.node.name"));

		// Read .installationinformation
		Boolean informationloaded = false;
		try {
			logger.log(Level.FINE, "InstallationInformationHelper Loading installation information");

			saveNewAppVersion(installData);

			loadInstallationInformation(installData);
			informationloaded = true;
			logger.log(Level.FINE, "InstallationInformationHelper Installation information loaded");
		} catch (Exception e) {
			logger.log(Level.FINE,
					"InstallationInformationHelper Installation information loading failed: " + e.getMessage());
			informationloaded = false;
		}

		// If old version 4.3.8, ...
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

		restoreNewVersion(installData);

		logger.log(Level.FINE, "After " + InstallData.INSTALLATION_INFORMATION + " component.node.name : "
				+ installData.getVariable("component.node.name"));

		logger.log(Level.FINE, "File " + InstallData.INSTALLATION_INFORMATION + " read.");

		return informationloaded;
	}

	public static void restoreNewVersion(com.izforge.izpack.api.data.InstallData installData) {
		String currentVersion = installData.getVariable("app-version");
		String newCurrentVersion = installData.getVariable("app-version-new");
		if (newCurrentVersion != null) {
			logger.log(Level.FINE, "InstallationInformationHelper Set current version 'app-version' (" + currentVersion
					+ ") from 'app-version-new' : " + currentVersion);
			installData.setVariable("app-version", newCurrentVersion);
		}

		String currentAPPVersion = installData.getVariable("APP_VER");
		String newCurrentAPPVersion = installData.getVariable("APP_VER_NEW");
		if (newCurrentAPPVersion != null) {
			logger.log(Level.FINE, "InstallationInformationHelper Set current version 'APP_VER' (" + currentAPPVersion
					+ ") from 'APP_VER_NEW' : " + currentAPPVersion);
			installData.setVariable("APP_VER", newCurrentAPPVersion);
		}
	}

	private static void saveNewAppVersion(com.izforge.izpack.api.data.InstallData installData) {
		String currentVersion = installData.getVariable("app-version");
		if (currentVersion != null) {
			installData.setVariable("app-version-new", currentVersion);
			logger.log(Level.FINE,
					"InstallationInformationHelper save current version 'app-version-new' : " + currentVersion);
		}
		String currentAPPVER = installData.getVariable("APP_VER");
		if (currentAPPVER != null) {
			installData.setVariable("APP_VER_NEW", currentAPPVER);
			logger.log(Level.FINE,
					"InstallationInformationHelper save current version 'APP_VER_NEW' : " + currentAPPVER);
		}
	}

	static boolean isIncompatibleInstallation(String installPath, boolean isReadInstallationInformation) {

		boolean result = false;

		if (isReadInstallationInformation) {

			ObjectInputStream oin = null;
			File installInfo = new File(installPath, InstallData.INSTALLATION_INFORMATION);
			if (installInfo.exists()) {
				FileInputStream fin = null;
				List<Pack> packsinstalled = null;
				try {
					fin = new FileInputStream(installInfo);
					oin = new ObjectInputStream(fin);

					packsinstalled = (List<Pack>) oin.readObject();

					for (Pack installedpack : packsinstalled) {
						logger.log(Level.FINE, "InstallationInformationHelper.isIncompatibleInstallation pack "
								+ installedpack.getName() + "");

					}
					logger.log(Level.FINE, "InstallationInformationHelper.isIncompatibleInstallation  Found "
							+ packsinstalled.size() + " installed packs");
				} catch (Exception e) {
					result = true;
					logger.warning(
							"InstallationInformationHelper.isIncompatibleInstallation  Could not read Pack installation information: "
									+ e.getMessage());
					// e.printStackTrace();
				}

				try {
					if (oin != null) {
						Properties variables = (Properties) oin.readObject();
						for (Object key : variables.keySet()) {
							logger.log(Level.FINE,
									"InstallationInformationHelper.isIncompatibleInstallation  Read variable " + key
											+ ": " + variables.get(key));
						}
					}
				} catch (Exception e) {
					result = true;
					logger.warning(
							"InstallationInformationHelper.isIncompatibleInstallation   Could not read Properties installation information: "
									+ e.getMessage());
				}

				// Previous version of izPack 4.3
				if (result == true) {

					try {
						fin = new FileInputStream(installInfo);
						oin = new ObjectInputStream(fin);
						List<com.izforge.izpack.Pack> packsinstalled2 = (List<com.izforge.izpack.Pack>) oin
								.readObject();

						for (com.izforge.izpack.Pack installedpack : packsinstalled2) {
							logger.log(Level.FINE,
									"InstallationInformationHelper.isIncompatibleInstallation - installedpack: "
											+ installedpack.name);
						}
						logger.log(Level.FINE,
								"InstallationInformationHelper.isIncompatibleInstallation - Found Legacy "
										+ packsinstalled.size() + " installed packs");

					} catch (Exception e) {
						logger.warning(
								"InstallationInformationHelper.isIncompatibleInstallation  Could not read Legacy 'Pack' installation information: "
										+ e.getMessage());
						// e.printStackTrace();
					} finally {

					}

					try {
						if (oin != null) {
							Properties variables = (Properties) oin.readObject();
							result = false;
							for (Object key : variables.keySet()) {

								logger.log(Level.FINE,
										"InstallationInformationHelper.isIncompatibleInstallation  Legacy variable : "
												+ key + ": " + variables.get(key));
							}

							logger.log(Level.FINE,
									"InstallationInformationHelper.isIncompatibleInstallation  InstallationInformation legacy read");
						}
					} catch (Exception e) {
						result = true;
						logger.warning(
								"InstallationInformationHelper.isIncompatibleInstallation  Could not read Legacy 'Properties' installation information: "
										+ e.getMessage());
					}
				}
			}

			if (oin != null) {
				try {
					oin.close();
				} catch (IOException ignored) {
				}
			}
		}
		logger.log(Level.FINE, "InstallationInformationHelper.isIncompatibleInstallation returns " + result + "");

		return result;
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

			List<com.izforge.izpack.api.data.Pack> packsinstalled = (List<com.izforge.izpack.api.data.Pack>) oin
					.readObject();
			try {
				for (com.izforge.izpack.api.data.Pack installedpack : packsinstalled) {
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
				logger.warning(
						"InstallationInformationHelper Could not read Pack installation information in current izPack version: "
								+ e.getMessage());
				throw e;
			}

			try {
				Properties variables = (Properties) oin.readObject();
				for (Object key : variables.keySet()) {
					if (((String) key).equals("app-version") || ((String) key).equals("APP_VER")
							|| ((String) key).equals("app-version-new") || ((String) key).equals("APP_VER_NEW") ) {
						installData.setVariable((String) key + "-old", (String) variables.get(key));
						logger.log(Level.FINE,
								"InstallationInformationHelper Skip variable : " + key + ": " + variables.get(key));
					} else {
						installData.setVariable((String) key, (String) variables.get(key));
						logger.log(Level.FINE,
								"InstallationInformationHelper  Set variable " + key + ": " + variables.get(key));
					}
				}
				installData.setVariable("information-read", "true");

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

		// Map<String, com.sage.izpack.Pack> readPacks = new HashMap<String,
		// com.sage.izpack.Pack>();
		Map<String, com.izforge.izpack.Pack> readPacks = new HashMap<String, com.izforge.izpack.Pack>();

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

			List<com.izforge.izpack.Pack> packsinstalled;
			ArrayList<Pack> packLists = new ArrayList<Pack>();
			try {
				packsinstalled = (List<com.izforge.izpack.Pack>) oin.readObject();

				for (com.izforge.izpack.Pack installedpack : packsinstalled) {
					readPacks.put(installedpack.name, installedpack);
					packLists.add(new Pack(installedpack.name, installedpack.id, installedpack.description, null,
							installedpack.dependencies, installedpack.required, installedpack.preselected,
							installedpack.loose, installedpack.excludeGroup, installedpack.uninstall, 0));

					logger.log(Level.FINE,
							"InstallationInformationHelper.loadLegacyInstallationInformation - installedpack: "
									+ installedpack.name);
				}
				// removeAlreadyInstalledPacks(installData.getSelectedPacks(), readPacks);

				// installData.setSelectedPacks(new ArrayList<Pack>());
				logger.log(Level.FINE, "InstallationInformationHelper.loadLegacyInstallationInformation - Found Legacy "
						+ packsinstalled.size() + " installed packs");
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
				logger.warning(
						"InstallationInformationHelper.loadLegacyInstallationInformation  Could not read Legacy 'Pack' installation information: "
								+ e1.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
				logger.warning(
						"InstallationInformationHelper.loadLegacyInstallationInformation  Could not read Legacy 'Pack' installation information: "
								+ e1.getMessage());
			} catch (Exception e) {
				logger.warning(
						"InstallationInformationHelper.loadLegacyInstallationInformation  Could not read Legacy 'Pack' installation information: "
								+ e.getMessage());
			} finally {
				installData.setSelectedPacks(packLists);
				logger.log(Level.FINE,
						"InstallationInformationHelper.loadLegacyInstallationInformation - setSelectedPacks: "
								+ packLists);
			}

			try {
				Properties variables = (Properties) oin.readObject();
				for (Object key : variables.keySet()) {

					if (((String) key).equals("app-version") || ((String) key).equals("APP_VER")) {
						installData.setVariable((String) key + "-old", (String) variables.get(key));
						logger.log(Level.FINE,
								"InstallationInformationHelper.loadLegacyInstallationInformation  Skip variable : "
										+ key + ": " + variables.get(key));
					} else {
						installData.setVariable((String) key, (String) variables.get(key));
						logger.log(Level.FINE,
								"InstallationInformationHelper.loadLegacyInstallationInformation  Set Legacy variable : "
										+ key + ": " + variables.get(key));
					}
				}

				logger.log(Level.FINE,
						"InstallationInformationHelper.loadLegacyInstallationInformation  writeInstallationInformation to fix legacy issue");
				// writeInstallationInformation(installData, installData.getSelectedPacks(),
				// installData.getVariables());
				writeInstallationInformation(installData, installData.getSelectedPacks(), installData.getVariables(),
						true);

			} catch (Exception e) {
				logger.warning(
						"InstallationInformationHelper.loadLegacyInstallationInformation  Could not read Legacy 'Properties' installation information: "
								+ e.getMessage());
			}
		}
	}

	private static void writeInstallationInformation(com.izforge.izpack.api.data.InstallData installData,
			List<Pack> selectedPacks, Variables variables, boolean forceWrite) throws IOException {

		if (!installData.getInfo().isWriteInstallationInformation() && !forceWrite) {
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
		oout.writeObject(variables.getProperties());
		fout.close();

		logger.log(Level.FINE, "Installation information saved: " + installationInfo.getAbsolutePath());
		// IOUtils.closeQuietly(oout);
		// IOUtils.closeQuietly(fout);

		// uninstallData.addFile(installationInfo.getAbsolutePath(), true);
	}

}
